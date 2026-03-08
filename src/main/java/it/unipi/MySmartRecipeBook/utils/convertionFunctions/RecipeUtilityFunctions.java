package it.unipi.MySmartRecipeBook.utils.convertionFunctions;

import it.unipi.MySmartRecipeBook.dto.IngredientDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.ChefPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.GraphRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.ShowRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.UserPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.ingredients.RecipeIngredient;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.ChefRecipeSummary;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.PendingRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.RecipeMongo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RecipeUtilityFunctions {

    public ShowRecipeDTO EntityToDto (RecipeMongo recipe){

        ShowRecipeDTO recipeDTO = new ShowRecipeDTO();
        recipeDTO.setTitle(recipe.getTitle());
        recipeDTO.setPresentation(recipe.getPresentation());
        recipeDTO.setCategory(recipe.getCategory());
        recipeDTO.setPrepTime(recipe.getPrepTime());
        recipeDTO.setDifficulty(recipe.getDifficulty());
        recipeDTO.setImageURL(recipe.getImageURL());
        recipeDTO.setPreparation(recipe.getPreparation());

        List<IngredientDTO> ingredients = new ArrayList<>();
        for(RecipeIngredient ingredient : recipe.getIngredients()){
            IngredientDTO ingredientDTO = new IngredientDTO();
            ingredientDTO.setName(ingredient.getName());
            ingredientDTO.setQuantity(ingredient.getQuantity());
            ingredients.add(ingredientDTO);
        }
        recipeDTO.setIngredients(ingredients);
        recipeDTO.setCreationDate(recipe.getCreationDate().toLocalDate());

        return recipeDTO;
    }

    public UserPreviewRecipeDTO EntityToUserDto (RecipeMongo recipe){

        UserPreviewRecipeDTO recipeDTO = new UserPreviewRecipeDTO();
        recipeDTO.setId(recipe.getId());
        recipeDTO.setTitle(recipe.getTitle());
        recipeDTO.setImageURL(recipe.getImageURL());
        recipeDTO.setChefName(recipe.getChef().getName() + " " + recipe.getChef().getSurname());

        return recipeDTO;
    }

    public ChefPreviewRecipeDTO EntityToChefDTO (RecipeMongo recipe){

        ChefPreviewRecipeDTO recipeDTO = new ChefPreviewRecipeDTO();
        recipeDTO.setId(recipe.getId());
        recipeDTO.setTitle(recipe.getTitle());
        recipeDTO.setImageURL(recipe.getImageURL());
        recipeDTO.setCreationDate(recipe.getCreationDate().toLocalDate());

        return recipeDTO;
    }


    public ChefRecipeSummary recipeToChefRecipe (RecipeMongo recipeMongo){

        ChefRecipeSummary recipe = new ChefRecipeSummary();
        recipe.setId(recipeMongo.getId());
        recipe.setTitle(recipeMongo.getTitle());
        recipe.setImageURL(recipeMongo.getImageURL());
        recipe.setCreationDate(recipeMongo.getCreationDate());

        return recipe;
    }

    // Quando l'admin approva una ricetta e deve essere inserita nel DB, dobbiamo trasformarla in una recipeMongo,
    // aggiungendo il campo numSaves inizializzato a 0
    public RecipeMongo baseToMongoRecipe(PendingRecipe recipe){

        RecipeMongo full_recipe = new RecipeMongo();
        full_recipe.setTitle(recipe.getTitle());
        full_recipe.setPresentation(recipe.getPresentation());
        full_recipe.setCategory(recipe.getCategory());
        full_recipe.setPrepTime(recipe.getPrepTime());
        full_recipe.setPreparation(recipe.getPreparation());
        full_recipe.setDifficulty(recipe.getDifficulty());
        full_recipe.setImageURL(recipe.getImageURL());
        full_recipe.setChef(recipe.getChef());
        full_recipe.setIngredients(recipe.getIngredients());
        full_recipe.setCreationDate(recipe.getCreationDate());
        full_recipe.setNumSaves(0);

        return full_recipe;
    }

    public GraphRecipeDTO MongoToNeo4jGraph(RecipeMongo recipe){

        GraphRecipeDTO recipeNeo4j = new GraphRecipeDTO();
        recipeNeo4j.setId(recipe.getId());
        recipeNeo4j.setTitle(recipe.getTitle());

        List<IngredientDTO> ingredients = new ArrayList<>();
        for(RecipeIngredient ingredient : recipe.getIngredients()){
            IngredientDTO ingredientDTO = new IngredientDTO();
            ingredientDTO.setName(ingredient.getName());
            ingredientDTO.setQuantity(ingredient.getQuantity());
            ingredients.add(ingredientDTO);
        }

        recipeNeo4j.setIngredients(ingredients);
        recipeNeo4j.setChefId(recipe.getChef().getId());
        recipeNeo4j.setImgURL(recipe.getImageURL());

        return recipeNeo4j;
    }

}
