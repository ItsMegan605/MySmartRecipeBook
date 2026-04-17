package it.unipi.MySmartRecipeBook.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.MySmartRecipeBook.dto.recipe.*;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.RecipeMongo;
import it.unipi.MySmartRecipeBook.utils.conversionFunctions.RecipeUtilityFunctions;
import org.springframework.beans.factory.annotation.Value;
import it.unipi.MySmartRecipeBook.repository.Mongo.RecipeMongoRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCluster;

import java.util.List;
import java.util.NoSuchElementException;

import static it.unipi.MySmartRecipeBook.utils.parameters.Categories.CATEGORIES;

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
    private final RecipeUtilityFunctions convertions;
    private JedisCluster jedisCluster;
    private final ObjectMapper objectMapper;

    public RecipeService(RecipeMongoRepository recipeRepository, RecipeUtilityFunctions convertions,  JedisCluster jedisCluster, ObjectMapper objectMapper) {
        this.recipeRepository = recipeRepository;
        this.convertions = convertions;
        this.jedisCluster = jedisCluster;
        this.objectMapper = objectMapper;
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

    // Si può veramente fare? //TODO: togliere?
    /*public void deleteRecipe(String recipeId) {
        if (!recipeRepository.existsById(recipeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found");
        }
        recipeRepository.deleteById(recipeId);
        /* Manca l'eliminazione da Neo4j e bisogna vedere se anche da Redis
    }*/

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
    public SliceRecipeDTO getChefRecipePage(int pageNumber, String chefId){

        if(pageNumber <= 0){
            throw new IllegalArgumentException("Invalid parameters");
        }

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSizeChef, Sort.by("totalSaves").descending());
        Slice<RecipeMongo> matching_recipes = recipeRepository.findByChef_Id(chefId, pageable);

        if (matching_recipes.isEmpty()){
            return new SliceRecipeDTO<>(List.of(), false, false);
        }

        List<UserPreviewRecipeDTO> recipesDTO = convertions.EntityToUserDto(matching_recipes.getContent());
        boolean hasNext = matching_recipes.hasNext();
        boolean hasPrevious = matching_recipes.hasPrevious();

        return new SliceRecipeDTO<>(recipesDTO, hasNext, hasPrevious);
    }
}

//TODO: a sliceRecipe ho aggiunto <>
