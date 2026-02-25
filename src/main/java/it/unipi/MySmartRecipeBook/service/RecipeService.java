package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.recipe.ShowRecipeDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.*;
import it.unipi.MySmartRecipeBook.utils.RecipeUtilityFunctions;
import org.springframework.beans.factory.annotation.Value;
import it.unipi.MySmartRecipeBook.dto.recipe.ChefPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.UserPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.repository.RecipeMongoRepository;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

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


    private static final List<String> VALID_CATEGORIES = List.of(
            "vegan",
            "dairy-free",
            "gluten-free",
            "egg-free",
            "main-course",
            "second-course",
            "dessert"
    );


    private final RecipeMongoRepository recipeRepository;
    private final RecipeUtilityFunctions convertions;
    public RecipeService(RecipeMongoRepository recipeRepository, RecipeUtilityFunctions convertions) {
        this.recipeRepository = recipeRepository;
        this.convertions = convertions;
    }


    public ShowRecipeDTO getRecipeById(String id){

        RecipeMongo full_recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        return convertions.EntityToDto(full_recipe);
    }

    public void deleteRecipe(String recipeId) {
        if (!recipeRepository.existsById(recipeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found");
        }
        recipeRepository.deleteById(recipeId);
        /* Manca l'eliminazione da Neo4j e bisogna vedere se anche da Redis*/
    }

    public List<UserPreviewRecipeDTO> getRecipeByTitle(String title, int pageNumber){

        if(pageNumber <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid page number");
        }

        Pageable pageable = PageRequest.of(--pageNumber, pageSizeTitle);
        Slice<RecipeMongo> matching_recipes = recipeRepository.findByTitleContainingIgnoreCase(title, pageable);
        if (matching_recipes.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
        }

        List<UserPreviewRecipeDTO> recipes = new ArrayList<>();
        for (RecipeMongo full_recipe : matching_recipes){
            UserPreviewRecipeDTO recipeDTO = convertions.EntityToUserDto(full_recipe);
            recipes.add(recipeDTO);
        }
        return recipes;
    }

    public List<UserPreviewRecipeDTO> getNewestRecipe (int pageNumber){

        if(pageNumber <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid page number");
        }

        Pageable pageable = PageRequest.of(--pageNumber, pageSizeHome, Sort.by("creationDate").descending());
        Page<RecipeMongo> pageResult = recipeRepository.findAll(pageable);

        List<UserPreviewRecipeDTO> recipe_list = new ArrayList<>();
        for (RecipeMongo recipe: pageResult.getContent()){
            UserPreviewRecipeDTO recipeDTO = convertions.EntityToUserDto(recipe);
            recipe_list.add(recipeDTO);
        }
        return recipe_list;
    }

    public List<UserPreviewRecipeDTO> getByCategory (int pageNumber, String filter){

        if(pageNumber <= 0 || !VALID_CATEGORIES.contains(filter)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameters");
        }

        Pageable pageable = PageRequest.of(--pageNumber, pageSizeCategory);
        Slice<RecipeMongo> matching_list = recipeRepository.findByCategory(filter, pageable);


        List<UserPreviewRecipeDTO> recipe_list = new ArrayList<>();
        for (RecipeMongo recipe: matching_list){
            UserPreviewRecipeDTO recipeDTO = convertions.EntityToUserDto(recipe);
            recipe_list.add(recipeDTO);
        }
        return recipe_list;
    }

    /* Per ora sono stati ordinati per data ma andrebbero ordinate per popolarità*/
    public List<ChefPreviewRecipeDTO> getChefRecipePage(int pageNumber, String chefName){

        if(pageNumber <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameters");
        }

        Pageable pageable = PageRequest.of(--pageNumber, pageSizeChef, Sort.by("totalSaves").descending());
        Slice<RecipeMongo> matching_recipes = recipeRepository.findByChefName(chefName, pageable);

        if (matching_recipes.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
        }

        List<ChefPreviewRecipeDTO> recipe_list = new ArrayList<>();
        for (RecipeMongo recipe: matching_recipes){
            ChefPreviewRecipeDTO recipeDTO = convertions.EntityToChefDTO(recipe);
            recipe_list.add(recipeDTO);
        }
        return recipe_list;
    }
}

