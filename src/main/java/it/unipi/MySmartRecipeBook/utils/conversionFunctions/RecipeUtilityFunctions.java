package it.unipi.MySmartRecipeBook.utils.conversionFunctions;

import it.unipi.MySmartRecipeBook.dto.IngredientDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.*;
import it.unipi.MySmartRecipeBook.dto.users.ChefInfoDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.ingredients.RecipeIngredient;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.*;
import it.unipi.MySmartRecipeBook.model.Mongo.users.ReducedChef;
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
    public ShowRecipeDTO entityToDto (RecipeMongo recipe){

        ShowRecipeDTO recipeDTO = new ShowRecipeDTO();
        recipeDTO.setMongoId(recipe.getId());
        recipeDTO.setTitle(recipe.getTitle());
        recipeDTO.setPresentation(recipe.getPresentation());
        recipeDTO.setCategory(recipe.getCategory());
        recipeDTO.setPrepTime(recipe.getPrepTime());
        recipeDTO.setDifficulty(recipe.getDifficulty());
        recipeDTO.setImageURL(recipe.getImageURL());
        recipeDTO.setPreparation(recipe.getPreparation());
        recipeDTO.setChefId(recipe.getChef().getId());
        recipeDTO.setChef(recipe.getChef().getName() + " " + recipe.getChef().getSurname());

        List<IngredientDTO> ingredients = ingredientsConversion(recipe.getIngredients());

        recipeDTO.setIngredients(ingredients);
        recipeDTO.setCreationDate(recipe.getCreationDate().toLocalDate());

        return recipeDTO;
    }

    /**
     * Converts recipeIngredients to IngredientsDTO
     * @param ingredientsRecipe recipe ingredients
     * @return list of converted ingredients
     */
    private List<IngredientDTO> ingredientsConversion (List<RecipeIngredient> ingredientsRecipe){
        List<IngredientDTO> ingredients = new ArrayList<>();
        for(RecipeIngredient ingredient : ingredientsRecipe){
            IngredientDTO ingredientDTO = new IngredientDTO();
            ingredientDTO.setName(ingredient.getName());
            ingredientDTO.setQuantity(ingredient.getQuantity());
            ingredients.add(ingredientDTO);
        }
        return ingredients;
    }

    /**
     * Converts a list of RecipeMongo entities into a list of UserPreviewRecipeDTOs.
     * @param recipes the list of mongo recipes
     * @return the list of user preview DTOs
     */
    public List<UserPreviewRecipeDTO> entityToUserDto (List<RecipeMongo> recipes){

        List<UserPreviewRecipeDTO> recipesDTO = new ArrayList<>();
        for(RecipeMongo recipe : recipes) {

            UserPreviewRecipeDTO recipeDTO = new UserPreviewRecipeDTO();
            recipeDTO.setId(recipe.getId());
            recipeDTO.setTitle(recipe.getTitle());
            recipeDTO.setImageURL(recipe.getImageURL());
            recipeDTO.setChefName(recipe.getChef().getName() + " " + recipe.getChef().getSurname());
            recipeDTO.setChefId(recipe.getChef().getId());
            Integer numSaves = recipe.getNumSaves();
            if (numSaves == null ){
                numSaves = 0;
            }
            recipeDTO.setNumSaves(numSaves);
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
     * Converts a RecipeMongo entity into a ChefRecipeSummary for the popular recipes
     * @param recipeMongo the mongo recipe
     * @return the chef recipe summary
     */
    public ChefRecipeSummary recipeToChefPopular (RecipeMongo recipeMongo){

        ChefRecipeSummary recipe = new ChefRecipeSummary();
        recipe.setId(recipeMongo.getId());
        recipe.setTitle(recipeMongo.getTitle());
        recipe.setImageURL(recipeMongo.getImageURL());
        recipe.setCreationDate(recipeMongo.getCreationDate());
        recipe.setNumSaves(recipeMongo.getNumSaves()+1);

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

        return recipeNeo4j;
    }


    /**
     * Creates a AdminPendingRecipe from a creation DTO and Chef info.
     * @param dto the recipe creation data
     * @param chefDTO the chef information
     * @return the AdminPendingRecipe entity
     */
    public AdminPendingRecipe createBaseRecipe (CreateRecipeDTO dto, ChefInfoDTO chefDTO, String recipeId){

        AdminPendingRecipe recipe = new AdminPendingRecipe();
        recipe.setId(recipeId);
        recipe.setTitle(dto.getTitle());
        recipe.setImageURL(dto.getImageURL());
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
    public PendingRecipe recipeToChefRecipe (AdminPendingRecipe recipe, String recipeId){

        PendingRecipe full_recipe = new PendingRecipe();
        full_recipe.setId(recipeId);
        full_recipe.setTitle(recipe.getTitle());
        full_recipe.setImageURL(recipe.getImageURL());
        full_recipe.setCreationDate(recipe.getCreationDate());

        return full_recipe;
    }

    /**
     * Converts a AdminPendingRecipe into a ChefPreviewRecipeDTO using a temporary ID.
     * @param recipe the pending recipe
     * @return the preview DTO
     */
    public PendingRecipeChefDTO baseToChefDTO(AdminPendingRecipe recipe){

        PendingRecipeChefDTO recipeDTO = new PendingRecipeChefDTO();

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
    public List<ChefPreviewRecipeDTO> chefListToSummaryList(List<ChefRecipeSummary> recipesList) {

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
    public List<ChefPreviewRecipeDTO> mongoListToChefPreview(List<RecipeMongo> recipesToConvert) {

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

    /**
     * Converts a list of pending recipes into a list of pending recipe DTOs for the chef preview.
     * @param recipes the list of pending recipes to convert
     * @return a list of PendingRecipeChefDTO containing the preview details
     */
    public List<PendingRecipeChefDTO> ChefPreviewToPendingChefRecipe(List<PendingRecipe> recipes) {
        List<PendingRecipeChefDTO> recipesPreview = new ArrayList<>();
        for (PendingRecipe recipe : recipes) {
            PendingRecipeChefDTO dto = new PendingRecipeChefDTO();
            dto.setId(recipe.getId());
            dto.setTitle(recipe.getTitle());
            dto.setImageURL(recipe.getImageURL());
            dto.setCreationDate(recipe.getCreationDate().toLocalDate());
            recipesPreview.add(dto);
        }
        return recipesPreview;

    }

    /**
     * Converts an AdminPendingRecipe into an PendingRecipeDTO.
     * @param recipe the pending recipe
     * @return the pending recipe DTO
     */
    public PendingRecipeDTO pendingRecipeToAdminDTO(AdminPendingRecipe recipe) {

        PendingRecipeDTO recipeDTO = new PendingRecipeDTO();
        recipeDTO.setId(recipe.getId());
        recipeDTO.setTitle(recipe.getTitle());
        recipeDTO.setChef(recipe.getChef().getName() + " " + recipe.getChef().getSurname());
        recipeDTO.setCreationDate(recipe.getCreationDate().toLocalDate());
        recipeDTO.setChefId(recipe.getChef().getId());
        return recipeDTO;
    }

    /**
     * Converts a CreateRecipeDTO and ChefInfoDTO into a complete RecipeMongo entity.
     * The resulting recipe is initialized with a "PENDING" status and the current timestamp.
     * @param recipeDto the data transfer object containing the recipe creation details
     * @param chefInfo the data transfer object containing the chef's information
     * @return a fully populated RecipeMongo entity representing the new recipe
     */
    public RecipeMongo dtoToModel (CreateRecipeDTO recipeDto, ChefInfoDTO chefInfo) {

        RecipeMongo recipe = new RecipeMongo();
        recipe.setTitle(recipeDto.getTitle());
        recipe.setPresentation(recipeDto.getPresentation());
        recipe.setCategory(recipeDto.getCategory());
        recipe.setPrepTime(recipeDto.getPrepTime());
        recipe.setDifficulty(recipeDto.getDifficulty());
        recipe.setImageURL(recipeDto.getImageURL());
        recipe.setPreparation(recipeDto.getPreparation());

        ReducedChef chef = new ReducedChef();
        chef.setId(chefInfo.getId());
        chef.setName(chefInfo.getName());
        chef.setSurname(chefInfo.getSurname());
        

        recipe.setChef(chef);

        List<RecipeIngredient> ingredients = new ArrayList<>();
        for(IngredientDTO ingredientDTO : recipeDto.getIngredients()){
            RecipeIngredient ingredient = new RecipeIngredient();
            ingredient.setName(ingredientDTO.getName());
            ingredient.setQuantity(ingredientDTO.getQuantity());
            ingredients.add(ingredient);
        }
        recipe.setIngredients(ingredients);
        recipe.setCreationDate(LocalDateTime.now());
        recipe.setStatus("PENDING");

        return recipe;
    }

    /**
     * Converts a list of recipe entities into a list of category trend Data Transfer Objects (DTOs).
     *
     * This method iterates through the provided recipe entities and maps their properties
     * to the corresponding fields in the DTO. Notably, it extracts the nested chef information
     * to set the chef ID and concatenates the chef's first and last name into a single full name string.
     *
     * @param recipes the list of {@link RecipeMongo} entities to be converted
     * @return a list of {@link TopRecipeByCategoryDTO} containing the formatted recipe data
     */
    public List<TopRecipeByCategoryDTO> entityToCategoryTrend(List<RecipeMongo> recipes) {
        List<TopRecipeByCategoryDTO> recipesPreview = new ArrayList<>();
        for (RecipeMongo recipe : recipes) {
            TopRecipeByCategoryDTO dto = new TopRecipeByCategoryDTO();
            dto.setId(recipe.getId());
            dto.setTitle(recipe.getTitle());
            dto.setCategory(recipe.getCategory());
            dto.setImageURL(recipe.getImageURL());
            dto.setNumSaves(recipe.getNumSaves());
            dto.setChefId(recipe.getChef().getId());
            dto.setChefName(recipe.getChef().getName() + " " + recipe.getChef().getSurname());
            recipesPreview.add(dto);
        }
        return recipesPreview;

    }
}
