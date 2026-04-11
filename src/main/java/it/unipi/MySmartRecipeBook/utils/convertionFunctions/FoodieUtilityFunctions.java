package it.unipi.MySmartRecipeBook.utils.convertionFunctions;

import it.unipi.MySmartRecipeBook.dto.IngredientDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.FoodiePreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.ShowRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.users.RegistedUserDTO;
import it.unipi.MySmartRecipeBook.dto.users.RegistedUserInfoDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.UserPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.ingredients.RecipeIngredient;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.ChefRecipeSummary;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Foodie;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.RecipeMongo;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.FoodieRecipeSummary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * * Utility class for Foodie-related entity and DTO conversions.
 */
@Component
public class FoodieUtilityFunctions {

    private final PasswordEncoder passwordEncoder;

    public FoodieUtilityFunctions(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Converts a registration DTO into a Foodie entity, encrypting the password.
     * @param foodieDTO the registration data
     * @return the Foodie entity
     */
    public Foodie createFoodieEntity (RegistedUserDTO foodieDTO){

        Foodie foodie = new Foodie();
        foodie.setUsername(foodieDTO.getUsername());
        foodie.setEmail(foodieDTO.getEmail());
        foodie.setPassword(passwordEncoder.encode(foodieDTO.getPassword()));

        foodie.setName(foodieDTO.getName());
        foodie.setSurname(foodieDTO.getSurname());
        foodie.setBirthDate(foodieDTO.getBirthdate());
        foodie.setRegistrationDate(new Date());

        return foodie;
    }

    /**
     * Converts a Foodie entity to a DTO for profile display.
     * @param foodie the Foodie entity
     * @return the registered user info DTO
     */
    public RegistedUserInfoDTO entityToFoodieDTO (Foodie foodie) {

        return new RegistedUserInfoDTO(
                foodie.getUsername(),
                foodie.getName(),
                foodie.getSurname(),
                foodie.getEmail(),
                foodie.getBirthDate()
        );
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
     * Converts a RecipeMongo entity into a ChefRecipeSummary.
     * @param recipeMongo the mongo recipe
     * @return the chef recipe summary
     */
    public ChefRecipeSummary entityToChefRecipe (RecipeMongo recipeMongo) {

        ChefRecipeSummary recipe = new ChefRecipeSummary();
        recipe.setId(recipeMongo.getId());
        recipe.setTitle(recipeMongo.getTitle());
        recipe.setImageURL(recipeMongo.getImageURL());
        recipe.setCreationDate(recipeMongo.getCreationDate());
        recipe.setNumSaves(recipeMongo.getNumSaves());

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
}
