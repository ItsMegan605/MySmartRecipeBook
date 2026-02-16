package it.unipi.MySmartRecipeBook.utils;

import it.unipi.MySmartRecipeBook.dto.recipe.ChefPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.StandardRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.UserPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.AdminRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.ChefRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.ChefRecipeSummary;
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
        recipeDTO.setId(recipe.getId());
        recipeDTO.setTitle(recipe.getTitle());
        recipeDTO.setPresentation(recipe.getPresentation());
        recipeDTO.setImageURL(recipe.getImageURL());
        //recipeDTO.setChefName(recipe.getChefName());

        return recipeDTO;
    }

    public ChefPreviewRecipeDTO EntityToChefDTO (RecipeMongo recipe){

        ChefPreviewRecipeDTO recipeDTO = new ChefPreviewRecipeDTO();
        recipeDTO.setId(recipe.getId());
        recipeDTO.setTitle(recipe.getTitle());
        recipeDTO.setPresentation(recipe.getPresentation());
        recipeDTO.setImageURL(recipe.getImageURL());
        recipeDTO.setCreationDate(recipe.getCreationDate().toLocalDate());

        return recipeDTO;
    }



    public ChefRecipeSummary entityToReducedRecipe (ChefRecipe full_recipe){

        ChefRecipeSummary recipe = new ChefRecipeSummary();
        recipe.setMongoId(full_recipe.getId());
        recipe.setTitle(full_recipe.getTitle());
        recipe.setImageURL(full_recipe.getImageURL());
        recipe.setCreationDate(full_recipe.getCreationDate());
        recipe.setNumSaves(full_recipe.getNumSaves());

        return recipe;
    }

    public ChefRecipe entityToChefRecipe (RecipeMongo recipe){

        ChefRecipe full_recipe = new ChefRecipe();
        full_recipe.setId(recipe.getId());
        full_recipe.setTitle(recipe.getTitle());
        full_recipe.setPresentation(recipe.getPresentation());
        full_recipe.setCategory(recipe.getCategory());
        full_recipe.setPrepTime(recipe.getPrepTime());
        full_recipe.setPreparation(recipe.getPreparation());
        full_recipe.setDifficulty(recipe.getDifficulty());
        full_recipe.setImageURL(recipe.getImageURL());
        full_recipe.setIngredients(recipe.getIngredients());
        full_recipe.setCreationDate(recipe.getCreationDate());
        full_recipe.setNumSaves(0);

        return full_recipe;
    }

    public ChefRecipe adminToChefRecipe (AdminRecipe recipe){

        ChefRecipe full_recipe = new ChefRecipe();
        full_recipe.setId(recipe.getId());
        full_recipe.setTitle(recipe.getTitle());
        full_recipe.setPresentation(recipe.getPresentation());
        full_recipe.setCategory(recipe.getCategory());
        full_recipe.setPrepTime(recipe.getPrepTime());
        full_recipe.setPreparation(recipe.getPreparation());
        full_recipe.setDifficulty(recipe.getDifficulty());
        full_recipe.setImageURL(recipe.getImageURL());
        full_recipe.setIngredients(recipe.getIngredients());
        full_recipe.setCreationDate(recipe.getCreationDate());
        full_recipe.setNumSaves(0);

        return full_recipe;
    }

    public RecipeMongo adminToMongoRecipe (AdminRecipe recipe){

        RecipeMongo full_recipe = new RecipeMongo();
        //full_recipe.setId(recipe.getId());
        full_recipe.setTitle(recipe.getTitle());
        full_recipe.setPresentation(recipe.getPresentation());
        full_recipe.setCategory(recipe.getCategory());
        full_recipe.setPrepTime(recipe.getPrepTime());
        full_recipe.setPreparation(recipe.getPreparation());
        full_recipe.setDifficulty(recipe.getDifficulty());
        full_recipe.setImageURL(recipe.getImageURL());
        full_recipe.setIngredients(recipe.getIngredients());
        full_recipe.setCreationDate(recipe.getCreationDate());

        return full_recipe;
    }

    public ChefRecipe RecipeMongoToChefRecipe(RecipeMongo recipeMongo){
        ChefRecipe recipe = new ChefRecipe();
        recipe.setId(recipeMongo.getId());
        recipe.setTitle(recipeMongo.getTitle());
        recipe.setPresentation(recipeMongo.getPresentation());
        recipe.setCategory(recipeMongo.getCategory());
        recipe.setPrepTime(recipeMongo.getPrepTime());
        recipe.setPreparation(recipeMongo.getPreparation());
        recipe.setDifficulty(recipeMongo.getDifficulty());
        recipe.setImageURL(recipeMongo.getImageURL());
        recipe.setIngredients(recipeMongo.getIngredients());
        recipe.setCreationDate(recipeMongo.getCreationDate());
        recipe.setNumSaves(0);
        return recipe;
    }
}
