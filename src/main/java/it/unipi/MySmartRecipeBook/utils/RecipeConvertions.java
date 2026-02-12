package it.unipi.MySmartRecipeBook.utils;

import it.unipi.MySmartRecipeBook.dto.recipe.ChefPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.RecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.UserPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.RecipeMongo;
import org.springframework.stereotype.Component;

@Component
public class RecipeConvertions {

    public RecipeDTO EntityToDto (RecipeMongo recipe){

        RecipeDTO recipeDTO = new RecipeDTO();
        recipeDTO.setTitle(recipe.getTitle());
        recipeDTO.setDescription(recipe.getDescription());
        recipeDTO.setCategory(recipe.getCategory());
        recipeDTO.setPrepTime(recipe.getPrepTime());
        recipeDTO.setDifficulty(recipe.getDifficulty());
        recipeDTO.setImageURL(recipe.getImageURL());
        recipeDTO.setPreparation(recipe.getPreparation());
        recipeDTO.setIngredients(recipe.getIngredients());
        recipeDTO.setCreationDate(recipe.getCreationDate().toLocalDate());

        return recipeDTO;
    }

    public UserPreviewRecipeDTO EntityToUserDto (RecipeMongo recipe){

        UserPreviewRecipeDTO recipeDTO = new UserPreviewRecipeDTO();
        recipeDTO.setMongo_id(recipe.getId());
        recipeDTO.setTitle(recipe.getTitle());
        recipeDTO.setDescription(recipe.getDescription());
        recipeDTO.setImageURL(recipe.getImageURL());
        recipeDTO.setChefUsername(recipe.getChefName());

        return recipeDTO;
    }

    public ChefPreviewRecipeDTO EntityToChefDto (RecipeMongo recipe){

        ChefPreviewRecipeDTO recipeDTO = new ChefPreviewRecipeDTO();
        recipeDTO.setMongo_id(recipe.getId());
        recipeDTO.setTitle(recipe.getTitle());
        recipeDTO.setDescription(recipe.getDescription());
        recipeDTO.setImageURL(recipe.getImageURL());
        recipeDTO.setCreationDate(recipe.getCreationDate().toLocalDate());

        return recipeDTO;
    }


}
