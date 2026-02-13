package it.unipi.MySmartRecipeBook.utils;

import it.unipi.MySmartRecipeBook.dto.recipe.ChefPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.StandardRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.UserPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.RecipeMongo;
import org.springframework.stereotype.Component;

@Component
public class RecipeConvertions {

    public StandardRecipeDTO EntityToDto (RecipeMongo recipe){

        StandardRecipeDTO standardRecipeDTO = new StandardRecipeDTO();
        standardRecipeDTO.setTitle(recipe.getTitle());
        standardRecipeDTO.setPresentation(recipe.getPresentation());
        standardRecipeDTO.setCategory(recipe.getCategory());
        standardRecipeDTO.setPrepTime(recipe.getPrepTime());
        standardRecipeDTO.setDifficulty(recipe.getDifficulty());
        standardRecipeDTO.setImageURL(recipe.getImageURL());
        standardRecipeDTO.setPreparation(recipe.getPreparation());
        standardRecipeDTO.setIngredients(recipe.getIngredients());
        standardRecipeDTO.setCreationDate(recipe.getCreationDate().toLocalDate());

        return standardRecipeDTO;
    }

    public UserPreviewRecipeDTO EntityToUserDto (RecipeMongo recipe){

        UserPreviewRecipeDTO recipeDTO = new UserPreviewRecipeDTO();
        recipeDTO.setMongoId(recipe.getId());
        recipeDTO.setTitle(recipe.getTitle());
        recipeDTO.setPresentation(recipe.getPresentation());
        recipeDTO.setImageURL(recipe.getImageURL());
        //recipeDTO.setChefName(recipe.getChefName());

        return recipeDTO;
    }

    public ChefPreviewRecipeDTO EntityToChefDto (RecipeMongo recipe){

        ChefPreviewRecipeDTO recipeDTO = new ChefPreviewRecipeDTO();
        recipeDTO.setMongoId(recipe.getId());
        recipeDTO.setTitle(recipe.getTitle());
        recipeDTO.setPresentation(recipe.getPresentation());
        recipeDTO.setImageURL(recipe.getImageURL());
        recipeDTO.setCreationDate(recipe.getCreationDate().toLocalDate());

        return recipeDTO;
    }


}
