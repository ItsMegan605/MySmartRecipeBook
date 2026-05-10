package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.recipe.*;
import it.unipi.MySmartRecipeBook.dto.users.ChefPreviewDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.OldRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.RecipeMongo;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Chef;
import it.unipi.MySmartRecipeBook.repository.Mongo.ChefRepository;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import it.unipi.MySmartRecipeBook.utils.conversionFunctions.ChefUtilityFunctions;
import it.unipi.MySmartRecipeBook.utils.conversionFunctions.RecipeUtilityFunctions;
import org.springframework.beans.factory.annotation.Value;
import it.unipi.MySmartRecipeBook.repository.Mongo.RecipeMongoRepository;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static it.unipi.MySmartRecipeBook.utils.parameters.Parameters.CATEGORIES;

/**
 *Recipe Service and its business logic
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
    private final RecipeUtilityFunctions convertions;
    private final ChefUtilityFunctions chefConvertions;

    public RecipeService(RecipeMongoRepository recipeRepository, ChefRepository chefRepository,
                         RecipeUtilityFunctions convertions, ChefUtilityFunctions chefConvertions) {
        this.recipeRepository = recipeRepository;
        this.chefRepository = chefRepository;
        this.convertions = convertions;
        this.chefConvertions = chefConvertions;
    }


    /**
     * Method to get a recipe and its information
     * @param id - recipe id
     * @return the full recipe
     */
    public ShowRecipeDTO getRecipeById(String id){

        RecipeMongo full_recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Recipe not found"));

        return convertions.EntityToDto(full_recipe);
    }

    /**
     * Method to search recipes by title, paginated.
     * @param title - recipe title
     * @param pageNumber - paging
     * @return Recipe's list and their paging
     */
    public SliceRecipeDTO getRecipeByTitle(String title, int pageNumber){

        if(pageNumber <= 0){
            throw new IllegalArgumentException("Invalid page number");
        }

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSizeTitle);
        Slice<RecipeMongo> matching_recipes = recipeRepository.findByTitleContainingIgnoreCase(title, pageable);
        if (matching_recipes.isEmpty()){
            throw new NoSuchElementException("Not found");
        }
        
        List<UserPreviewRecipeDTO> recipesDTO = convertions.EntityToUserDto(matching_recipes.getContent());
        boolean hasNext = matching_recipes.hasNext();
        boolean hasPrevious = matching_recipes.hasPrevious();

        return new SliceRecipeDTO<>(recipesDTO, hasNext, hasPrevious);
    }

    /**
     * Method for the newest recipes on the app
     * @param pageNumber - paging
     * @return the latest recipes and the pagination
     */
    public SliceRecipeDTO getNewestRecipe (int pageNumber){

        if(pageNumber <= 0){
            throw new IllegalArgumentException("Invalid page number");
        }

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSizeHome, Sort.by("creationDate").descending());
        Slice <RecipeMongo> pageResult = recipeRepository.findAll(pageable);

        List<UserPreviewRecipeDTO> recipesDTO = convertions.EntityToUserDto(pageResult.getContent());
        boolean hasNext = pageResult.hasNext();
        boolean hasPrevious = pageResult.hasPrevious();
        return new SliceRecipeDTO<>(recipesDTO, hasNext, hasPrevious);
    }

    /**
     * Method to filter the recipes by category
     * @param pageNumber - paging
     * @param filter - page filter
     * @return recipes and filtering
     */
    public SliceRecipeDTO getByCategory (int pageNumber, String filter){

        if(pageNumber <= 0 || !CATEGORIES.contains(filter)){
            throw new IllegalArgumentException("Invalid parameters");
        }

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSizeCategory, Sort.by("creationDate").descending());
        Slice<RecipeMongo> matching_list = recipeRepository.findByCategory(filter, pageable);

        List<UserPreviewRecipeDTO> recipesDTO = convertions.EntityToUserDto(matching_list.getContent());
        boolean hasNext = matching_list.hasNext();
        boolean hasPrevious = matching_list.hasPrevious();

        return new SliceRecipeDTO<>(recipesDTO, hasNext, hasPrevious);
    }

    /**
     * Method to get recipes by chef
     * @param pageNumber - paging
     * @param chefId - id of the chef
     * @return recipes and their paging
     */
    public SliceRecipeDTO getChefRecipePage (int pageNumber, String chefId){

        Chef chef = chefRepository.findById(chefId)
                .orElseThrow(() -> new NoSuchElementException("Chef not found"));

        int start = (pageNumber-1)*pageSizeChef;
        int end = pageNumber*pageSizeChef;
        List<ChefPreviewRecipeDTO> content;
        boolean hasPrevious = true;
        if(pageNumber <= 3){

            if (chef.getNewRecipes() == null || chef.getNewRecipes().isEmpty()) {
                return new SliceRecipeDTO<>(null, false, false);
            }

            content = convertions.ChefListToSummaryList(chef.getNewRecipes().subList(start, end));
            hasPrevious = pageNumber == 1 ? false :  true;
        }

        else{

            List<OldRecipe> oldRecipes = chef.getOldRecipes().subList(start, end);
            List<String> ids = oldRecipes.stream().map(OldRecipe::getId).toList();
            List<RecipeMongo> recipes = recipeRepository.findByIdIn(ids);
            content = convertions.MongoListToChefPreview(recipes);
        }

        boolean hasNext = (chef.getTotalRecipes() > end) ? true : false;
        return  new SliceRecipeDTO<>(content, hasNext, hasPrevious);
    }


}

