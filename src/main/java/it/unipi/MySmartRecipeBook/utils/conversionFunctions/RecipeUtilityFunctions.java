package it.unipi.MySmartRecipeBook.utils.conversionFunctions;

import it.unipi.MySmartRecipeBook.dto.IngredientDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.*;
import it.unipi.MySmartRecipeBook.dto.users.ChefInfoDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.ingredients.RecipeIngredient;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.*;
import it.unipi.MySmartRecipeBook.model.Mongo.users.ReducedChef;
import it.unipi.MySmartRecipeBook.model.Neo4j.ChefNeo4j;
import it.unipi.MySmartRecipeBook.model.Neo4j.IngredientNeo4j;
import it.unipi.MySmartRecipeBook.model.Neo4j.RecipeNeo4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        recipeDTO.setMongoId(recipe.getId());
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
     * Converts a list of RecipeMongo entities to a list of ChefRecipeSummary DTOs.
     * @param recipesToConvert the list of mongo recipes
     * @return the list of summaries
     */
    public List<ChefRecipeSummary> MongoListToChefListSummary(List<RecipeMongo> recipesToConvert) {

        List<ChefRecipeSummary> chefRecipes = new ArrayList<>();

        for(RecipeMongo recipeMongo : recipesToConvert){
            ChefRecipeSummary recipe = recipeToChefRecipe(recipeMongo);
            chefRecipes.add(recipe);
        }

        return chefRecipes;
    }

    /**
     * Converts an approved AdminPendingRecipe into a final RecipeMongo entity.
     * Initializes the save counter to zero.
     * @param recipe the pending recipe
     * @return the final mongo recipe
     */
    public RecipeMongo baseToMongoRecipe(AdminPendingRecipe recipe){

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

    /**
     * Creates a AdminPendingRecipe from a creation DTO and Chef info.
     * @param dto the recipe creation data
     * @param chefDTO the chef information
     * @return the AdminPendingRecipe entity
     */
    public AdminPendingRecipe createBaseRecipe (CreateRecipeDTO dto, ChefInfoDTO chefDTO){

        AdminPendingRecipe recipe = new AdminPendingRecipe();
        recipe.setId(java.util.UUID.randomUUID().toString());
        recipe.setTitle(dto.getTitle());
        recipe.setCategory(dto.getCategory());
        recipe.setPreparation(dto.getPreparation());
        recipe.setPrepTime(dto.getPrepTime());
        recipe.setDifficulty(dto.getDifficulty());
        recipe.setPresentation(dto.getPresentation());
        recipe.setImageURL(dto.getImageURL());

        List<RecipeIngredient> ingredients = new ArrayList<>();
        for(IngredientDTO ingredientDTO : dto.getIngredients()){
            RecipeIngredient ingredient = new RecipeIngredient();
            ingredient.setName(ingredientDTO.getName());
            ingredient.setQuantity(ingredientDTO.getQuantity());
            ingredients.add(ingredient);
        }
        recipe.setIngredients(ingredients);
        recipe.setCreationDate(LocalDateTime.now());

        ReducedChef chef = new ReducedChef();
        chef.setId(chefDTO.getId());
        chef.setName(chefDTO.getName());
        chef.setSurname(chefDTO.getSurname());

        recipe.setChef(chef);

        return recipe;
    }

    /**
     * Converts a AdminPendingRecipe into a PendingRecipe: extra NumSaves field.
     * It removes redundant chef information.
     * @param recipe the pending recipe
     * @return the chef pending recipe summary
     */
    public PendingRecipe recipeToChefRecipe (AdminPendingRecipe recipe){

        PendingRecipe full_recipe = new PendingRecipe();
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

        return full_recipe;
    }

    /**
     * Converts a AdminPendingRecipe into a ChefPreviewRecipeDTO using a temporary ID.
     * @param recipe the pending recipe
     * @return the preview DTO
     */
    public ChefPreviewRecipeDTO baseToChefDTO(AdminPendingRecipe recipe){

        ChefPreviewRecipeDTO recipeDTO = new ChefPreviewRecipeDTO();

        /* In this case the id is the temporary one (not the one generated by Mongo) since the recipe has not
        been inserted in the recipes collection yet */
        recipeDTO.setId(recipe.getId());
        recipeDTO.setTitle(recipe.getTitle());
        recipeDTO.setImageURL(recipe.getImageURL());
        recipeDTO.setCreationDate(recipe.getCreationDate().toLocalDate());

        return recipeDTO;
    }

    /**
     * Converts a list of ChefRecipeSummary entities to a list of ChefPreviewRecipeDTOs.
     * @param recipesList the list of recipe summaries
     * @return the list of preview DTOs
     */
    public List<ChefPreviewRecipeDTO> ChefListToSummaryList(List<ChefRecipeSummary> recipesList) {

        List<ChefPreviewRecipeDTO> chefPreviewList = new ArrayList<>();
        for(ChefRecipeSummary recipe : recipesList){
            ChefPreviewRecipeDTO recipeDTO = new ChefPreviewRecipeDTO();

            recipeDTO.setId(recipe.getId());
            recipeDTO.setTitle(recipe.getTitle());
            recipeDTO.setImageURL(recipe.getImageURL());
            recipeDTO.setCreationDate(recipe.getCreationDate().toLocalDate());
            recipeDTO.setNumSaves(
                    recipe.getNumSaves() == null ? 0 : recipe.getNumSaves()
            );

            chefPreviewList.add(recipeDTO);
        }

        return  chefPreviewList;
    }


    /**
     * Converts a list of RecipeMongo entities directly to ChefPreviewRecipeDTOs.
     * @param recipesToConvert the list of mongo recipes
     * @return the list of preview DTOs
     */
    public List<ChefPreviewRecipeDTO> MongoListToChefPreview(List<RecipeMongo> recipesToConvert) {

        List<ChefPreviewRecipeDTO> chefRecipes = new ArrayList<>();

        for(RecipeMongo recipeMongo : recipesToConvert){
            ChefPreviewRecipeDTO recipe = new ChefPreviewRecipeDTO();

            recipe.setId(recipeMongo.getId());
            recipe.setTitle(recipeMongo.getTitle());
            recipe.setImageURL(recipeMongo.getImageURL());
            recipe.setCreationDate(recipeMongo.getCreationDate().toLocalDate());
            recipe.setNumSaves(recipeMongo.getNumSaves());
            chefRecipes.add(recipe);
        }

        return chefRecipes;
    }

    /**
     * Converts a list of PendingRecipe entities into a list of ChefPreviewRecipeDTOs.
     * @param recipes the list of chef pending recipes
     * @return the list of preview DTOs
     */
    public List<ChefPreviewRecipeDTO> PendingChefListToChefPreview(List<PendingRecipe> recipes) {
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

    /**
     * Converts a RecipeMongo entity into a FoodieRecipeSummary for saved recipes.
     * @param recipeMongo the mongo recipe
     * @return the foodie recipe summary
     */
    public FoodieRecipeSummary entityToReducedRecipe (RecipeMongo recipeMongo) {

        FoodieRecipeSummary recipe = new FoodieRecipeSummary();
        recipe.setId(recipeMongo.getId());
        recipe.setTitle(recipeMongo.getTitle());
        recipe.setImageURL(recipeMongo.getImageURL());
        recipe.setCategory(recipeMongo.getCategory());
        recipe.setDifficulty(recipeMongo.getDifficulty());
        recipe.setChef(recipeMongo.getChef());
        recipe.setSavingDate(LocalDate.now());

        return  recipe;
    }

    /**
     * Converts a list of FoodieRecipeSummary entities into a list of FoodiePreviewRecipeDTOs.
     * @param fullRecipes the list of foodie recipe summaries
     * @return the list of preview DTOs
     */
    public List<FoodiePreviewRecipeDTO> foodieSummaryToUserPreview (List<FoodieRecipeSummary> fullRecipes) {

        List<FoodiePreviewRecipeDTO> recipes = new ArrayList<>();

        for(FoodieRecipeSummary recipe: fullRecipes) {
            FoodiePreviewRecipeDTO userPreviewRecipeDTO = new FoodiePreviewRecipeDTO();
            userPreviewRecipeDTO.setId(recipe.getId());
            userPreviewRecipeDTO.setTitle(recipe.getTitle());
            userPreviewRecipeDTO.setImageURL(recipe.getImageURL());
            userPreviewRecipeDTO.setChefName(recipe.getChef().getName() + " " + recipe.getChef().getSurname());
            userPreviewRecipeDTO.setChefId(recipe.getChef().getId());
            recipes.add(userPreviewRecipeDTO);
        }

        return recipes;
    }
}
