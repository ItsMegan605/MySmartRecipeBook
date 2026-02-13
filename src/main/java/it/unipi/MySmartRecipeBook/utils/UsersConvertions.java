package it.unipi.MySmartRecipeBook.utils;

import it.unipi.MySmartRecipeBook.dto.foodie.StandardFoodieDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.BaseRecipeDTO;
import it.unipi.MySmartRecipeBook.model.Foodie;
import it.unipi.MySmartRecipeBook.model.Mongo.FoodieRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.RecipeMongo;
import it.unipi.MySmartRecipeBook.model.Mongo.FoodieRecipeSummary;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class UsersConvertions {

    public StandardFoodieDTO entityToFoodieDTO (Foodie foodie) {

        return new StandardFoodieDTO(
                foodie.getUsername(),
                foodie.getName(),
                foodie.getSurname(),
                foodie.getEmail(),
                foodie.getBirthdate()
        );
    }

    public FoodieRecipe dtoToFoodieRecipe (BaseRecipeDTO recipeDTO) {

        FoodieRecipe recipeMongo = new FoodieRecipe();
        recipeMongo.setId(recipeDTO.getMongoId());
        recipeMongo.setTitle(recipeDTO.getTitle());
        recipeMongo.setImageURL(recipeDTO.getImageURL());
        recipeMongo.setCategory(recipeDTO.getCategory());
        recipeMongo.setDifficulty(recipeDTO.getDifficulty());
        recipeMongo.setPrepTime(recipeDTO.getPrepTime());
        recipeMongo.setPresentation(recipeDTO.getPresentation());
        recipeMongo.setIngredients(recipeDTO.getIngredients());
        recipeMongo.setPreparation(recipeDTO.getPreparation());
        //recipeMongo.setChef(recipeDTO.getChefName());
        recipeMongo.setSavingDate(LocalDate.now());

        return recipeMongo;
    }

    public FoodieRecipeSummary entityToReducedRecipe (FoodieRecipe recipeMongo) {

        FoodieRecipeSummary recipe = new FoodieRecipeSummary();
        recipe.setId(recipeMongo.getId());
        recipe.setTitle(recipeMongo.getTitle());
        recipe.setImageURL(recipeMongo.getImageURL());
        recipe.setCategory(recipeMongo.getCategory());
        recipe.setDifficulty(recipeMongo.getDifficulty());
        recipe.setSavingDate(recipeMongo.getSavingDate());

        return  recipe;
    }

    public FoodieRecipe entityToFoodieEntity (RecipeMongo recipeMongo) {

        FoodieRecipe recipe = new FoodieRecipe();
        recipe.setId(recipeMongo.getId());
        recipe.setTitle(recipeMongo.getTitle());
        recipe.setImageURL(recipeMongo.getImageURL());
        recipe.setCategory(recipeMongo.getCategory());
        recipe.setDifficulty(recipeMongo.getDifficulty());
        recipe.setPrepTime(recipeMongo.getPrepTime());
        recipe.setPresentation(recipeMongo.getPresentation());
        recipe.setIngredients(recipeMongo.getIngredients());
        recipe.setPreparation(recipeMongo.getPreparation());
        //recipe.setChefName(recipeMongo.getChefName());
        recipe.setSavingDate(LocalDate.now());

        return recipe;
    }
}
