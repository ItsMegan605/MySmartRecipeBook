package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.recipe.*;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.RecipeMongo;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Chef;
import it.unipi.MySmartRecipeBook.repository.Mongo.ChefRepository;
import it.unipi.MySmartRecipeBook.utils.conversionFunctions.RecipeUtilityFunctions;
import org.springframework.beans.factory.annotation.Value;
import it.unipi.MySmartRecipeBook.repository.Mongo.RecipeMongoRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.swing.text.Document;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static it.unipi.MySmartRecipeBook.utils.parameters.Parameters.CATEGORIES;

/**
 * Recipe service that handles recipes' business logic operations
 */
@Service
public class RecipeService {

    @Value("${app.recipe.pag-size-title:5}")
    private int pageSizeTitle;

    @Value("${app.recipe.pag-size-category:10}")
    private int pageSizeCategory;

    @Value("${app.recipe.pag-size-home:10}")
    private int pageSizeHome;

    @Value("${app.recipe.pag-size-chef:5}")
    private int pageSizeChef;

    private final RecipeMongoRepository recipeRepository;
    private final ChefRepository chefRepository;
    private final RecipeUtilityFunctions recipeConversions;

    public RecipeService(RecipeMongoRepository recipeRepository, ChefRepository chefRepository,
                         RecipeUtilityFunctions recipeConversions) {
        this.recipeRepository = recipeRepository;
        this.chefRepository = chefRepository;
        this.recipeConversions = recipeConversions;
    }


    /**
     * Retrieves the detailed information of the specified recipe
     * @param recipeId the unique identifier of the recipe to visualize
     * @return a {@link ShowRecipeDTO} containing the detailed description of the specified recipe
     * @throws NoSuchElementException if the recipe is not found
     */
    public ShowRecipeDTO getRecipeById(String recipeId){

        RecipeMongo fullRecipe = recipeRepository.findApprovedById(recipeId)
                .orElseThrow(() -> new NoSuchElementException("Recipe not found"));

        return recipeConversions.entityToDto(fullRecipe);
    }


    /**
     * Retrieves all the recipes whose titles contain the specified substring. The result is paginated.
     * @param title the target substring to search for in the recipe titles
     * @param pageNumber the page number to retrieve
     * @return a {@link SliceRecipeDTO} containing a preview of the recipes, along with two boolean values indicating
     * the existence of previous or next pages
     * @throws IllegalArgumentException if the page number is zero or negative, or if the title is null or blank
     */
    public SliceRecipeDTO<UserPreviewRecipeDTO> getRecipeByTitle(String title, int pageNumber){

        if(pageNumber <= 0){
            throw new IllegalArgumentException("Invalid page number");
        }

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Search title cannot be null or empty");
        }

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSizeTitle);
        Slice<RecipeMongo> matchingRecipes = recipeRepository.findRecipesByTitle(title, "APPROVED", pageable);
        return buildSliceDto(matchingRecipes);
    }


    /**
     * Retrieves all the newest recipes published on the application. The result is paginated.
     * @param pageNumber the page number to retrieve
     * @return a {@link SliceRecipeDTO} containing a preview of the recipes, along with two boolean values indicating
     * the existence of previous or next pages
     * @throws IllegalArgumentException if the page number is zero or negative
     */
    public SliceRecipeDTO<UserPreviewRecipeDTO> getNewestRecipe (int pageNumber){

        if(pageNumber <= 0){
            throw new IllegalArgumentException("Invalid page number");
        }

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSizeHome, Sort.by("creationDate").descending());
        Slice <RecipeMongo> pageResult = recipeRepository.findByStatus("APPROVED", pageable);
        return buildSliceDto(pageResult);
    }


    /**
     * Retrieves all the recipes of a specified category. The result is paginated.
     * @param pageNumber the page number to retrieve
     * @param filter the specified category to search for
     * @return a {@link SliceRecipeDTO} containing a preview of the recipes, along with two boolean values indicating
     * the existence of previous or next pages
     * @throws IllegalArgumentException if the page number is zero or negative, or if the category is null or invalid
     */
    public SliceRecipeDTO<UserPreviewRecipeDTO> getByCategory (int pageNumber, String filter){

        if (pageNumber <= 0) {
            throw new IllegalArgumentException("Invalid page number");
        }

        if (filter == null || !CATEGORIES.contains(filter)) {
            throw new IllegalArgumentException("Invalid category filter");
        }

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSizeCategory, Sort.by("creationDate").descending());
        Slice<RecipeMongo> matchingList = recipeRepository.findByCategory(filter, "APPROVED", pageable);

        return buildSliceDto(matchingList);
    }


    /**
     * Auxiliary method that converts a {@code Slice<RecipeMongo>} (a list of recipes that match specific parameters)
     * into a {@link SliceRecipeDTO} containing the previews of the recipes.
     * @param sliceResult the {@link Slice} of MongoDB recipes to convert
     * @return a {@link SliceRecipeDTO} containing a preview of the recipes, along with two boolean values indicating
     * the existence of previous or next pages
     */
    private SliceRecipeDTO<UserPreviewRecipeDTO> buildSliceDto(Slice<RecipeMongo> sliceResult) {

        List<UserPreviewRecipeDTO> recipesDTO = new ArrayList<>();
        if(!sliceResult.isEmpty()) {
            recipesDTO = recipeConversions.entityToUserDto(sliceResult.getContent());
        }
        return new SliceRecipeDTO<>(recipesDTO, sliceResult.hasNext(), sliceResult.hasPrevious());
    }


    /**
     * Retrieves a paginated list of a chef's published recipes, typically displayed when a user visits the chef's public profile.
     * @param pageNumber the page number to retrieve
     * @param chefId the unique identifier of the chef whose recipes we want to retrieve
     * @return a {@link SliceRecipeDTO} containing the previews of the recipes, along with two boolean values indicating
     * the existence of previous or next pages
     * @throws IllegalArgumentException if the page number is zero or negative
     * @throws NoSuchElementException if the chef is not found
     */
    public SliceRecipeDTO<ChefPreviewRecipeDTO> getChefRecipePage (int pageNumber, String chefId){

        if(pageNumber <= 0){
            throw new IllegalArgumentException("Invalid page number");
        }

        Chef chef = chefRepository.findApprovedById(chefId)
                .orElseThrow(() -> new NoSuchElementException("Chef not found"));

        int start = (pageNumber-1) * pageSizeChef;
        int end = pageNumber * pageSizeChef;

        List<ChefPreviewRecipeDTO> content = new ArrayList<>();
        boolean hasPrevious = pageNumber > 1;

        if(pageNumber <= 3){

            if (chef.getNewRecipes() == null || chef.getNewRecipes().isEmpty()) {
                return new SliceRecipeDTO<>(new ArrayList<>(), false, false);
            }

            if (start < chef.getNewRecipes().size()) {
                int finalEnd = Math.min(end, chef.getNewRecipes().size());
                content = recipeConversions.chefListToSummaryList(chef.getNewRecipes().subList(start, finalEnd));
            }
        }

        else{
            if(chef.getOldRecipes() != null && !chef.getOldRecipes().isEmpty()) {
                int oldRecipeStart = start - pageSizeChef * 3;
                int oldRecipeEnd = end - pageSizeChef * 3;

                if(oldRecipeStart < chef.getOldRecipes().size()) {
                    int finalEnd = Math.min(oldRecipeEnd, chef.getOldRecipes().size());
                    List<String> oldRecipesIds = chef.getOldRecipes().subList(oldRecipeStart, finalEnd);
                    List<RecipeMongo> recipes = recipeRepository.findByIdIn(oldRecipesIds);
                    content = recipeConversions.mongoListToChefPreview(recipes);
                }
            }
        }

        int totRecipes = chef.getTotalRecipes() == null ? 0 :  chef.getTotalRecipes();
        boolean hasNext = totRecipes > end;
        return new SliceRecipeDTO<>(content, hasNext, hasPrevious);
    }


    public List<TopRecipeByCategoryDTO> getCategoryTrend() {
        return recipeRepository.findMostSavedRecipePerCategory();
    }

}

