package it.unipi.MySmartRecipeBook.utils.convertionFunctions;

import it.unipi.MySmartRecipeBook.dto.IngredientDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.*;
import it.unipi.MySmartRecipeBook.model.Mongo.ingredients.RecipeIngredient;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.ChefRecipeSummary;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.PendingRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.RecipeMongo;
import it.unipi.MySmartRecipeBook.model.Neo4j.ChefNeo4j;
import it.unipi.MySmartRecipeBook.model.Neo4j.IngredientNeo4j;
import it.unipi.MySmartRecipeBook.model.Neo4j.RecipeNeo4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * * Utility class for Recipe-related entity, DTO, and Graph conversions.
 */
@Component
public class RecipeUtilityFunctions {

    /**
     * Converts a RecipeMongo entity into a ShowRecipeDTO for detailed viewing.
     * @param recipe the mongo recipe
     * @return the detailed recipe DTO
     */
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

    /**
     * Converts a list of RecipeMongo entities into a list of UserPreviewRecipeDTOs.
     * @param recipes the list of mongo recipes
     * @return the list of user preview DTOs
     */
    public List<UserPreviewRecipeDTO> EntityToUserDto (List<RecipeMongo> recipes){

        List<UserPreviewRecipeDTO> recipesDTO = new ArrayList<>();
        for(RecipeMongo recipe : recipes) {
            UserPreviewRecipeDTO recipeDTO = new UserPreviewRecipeDTO();
            recipeDTO.setId(recipe.getId());
            recipeDTO.setTitle(recipe.getTitle());
            recipeDTO.setImageURL(recipe.getImageURL());
            recipeDTO.setChefName(recipe.getChef().getName() + " " + recipe.getChef().getSurname());
            recipeDTO.setChefId(recipe.getChef().getId());
            recipeDTO.setNumSaves(recipe.getNumSaves());
            recipesDTO.add(recipeDTO);
        }
        return recipesDTO;
    }


    /**
     * Converts a RecipeMongo entity into a ChefRecipeSummary.
     * @param recipeMongo the mongo recipe
     * @return the chef recipe summary
     */
    public ChefRecipeSummary recipeToChefRecipe (RecipeMongo recipeMongo){

        ChefRecipeSummary recipe = new ChefRecipeSummary();
        recipe.setId(recipeMongo.getId());
        recipe.setTitle(recipeMongo.getTitle());
        recipe.setImageURL(recipeMongo.getImageURL());
        recipe.setCreationDate(recipeMongo.getCreationDate());

        return recipe;
    }

    /**
     * Converts an approved PendingRecipe into a final RecipeMongo entity.
     * Initializes the save counter to zero.
     * @param recipe the pending recipe
     * @return the final mongo recipe
     */
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

    //TODO: togliamo allora??

    /* RIDONDANTE
    public List<ChefPreviewRecipeDTO> PendingListToChefPreview(List<PendingRecipe> recipes) {
        List<ChefPreviewRecipeDTO> result = new ArrayList<>();
        for (PendingRecipe recipe : recipes) {
            ChefPreviewRecipeDTO dto = new ChefPreviewRecipeDTO();
            dto.setId(recipe.getId());
            dto.setTitle(recipe.getTitle());
            dto.setImageURL(recipe.getImageURL());
            dto.setCreationDate(recipe.getCreationDate().toLocalDate());
            result.add(dto);
        }
        return result;
    }
    */

    /**
     * Converts a RecipeMongo entity into a GraphRecipeDTO for Neo4j synchronization.
     * @param recipe the mongo recipe
     * @return the graph recipe DTO
     */
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
        recipeNeo4j.setCategory(recipe.getCategory());

        return recipeNeo4j;
    }

    /**
     * Converts a RecipeMongo entity into a RecipeNeo4j node entity.
     * @param recipe the mongo recipe
     * @return the Neo4j recipe entity
     */
    public RecipeNeo4j MongoToNeo4j(RecipeMongo recipe){

        RecipeNeo4j recipeNeo4j = new RecipeNeo4j();
        recipeNeo4j.setMongoId(recipe.getId());
        recipeNeo4j.setTitle(recipe.getTitle());

        List<IngredientNeo4j> ingredients = new ArrayList<>();
        for(RecipeIngredient ingredient : recipe.getIngredients()){
            IngredientNeo4j ingredientDTO = new IngredientNeo4j();
            ingredientDTO.setName(ingredient.getName());
            ingredients.add(ingredientDTO);
        }
        recipeNeo4j.setIngredients(ingredients);

        ChefNeo4j chef = new ChefNeo4j();
        chef.setMongoId(recipe.getChef().getId());
        chef.setName(recipe.getChef().getName());
        chef.setSurname(recipe.getChef().getSurname());
        recipeNeo4j.setChef(chef);

        recipeNeo4j.setImageURL(recipe.getImageURL());
        recipeNeo4j.setCategory(recipe.getCategory());

        return recipeNeo4j;
    }
}
