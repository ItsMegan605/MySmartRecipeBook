package it.unipi.MySmartRecipeBook.utils.convertionFunctions;

import it.unipi.MySmartRecipeBook.dto.recipe.FoodiePreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.users.RegistedUserDTO;
import it.unipi.MySmartRecipeBook.dto.users.RegistedUserInfoDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.UserPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Foodie;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.RecipeMongo;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.FoodieRecipeSummary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class FoodieUtilityFunctions {

    private final PasswordEncoder passwordEncoder;

    public FoodieUtilityFunctions(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }


    public Foodie createFoodieEntity (RegistedUserDTO foodieDTO){

        Foodie foodie = new Foodie();
        foodie.setUsername(foodieDTO.getUsername());
        foodie.setEmail(foodieDTO.getEmail());
        foodie.setPassword(passwordEncoder.encode(foodieDTO.getPassword()));

        foodie.setName(foodieDTO.getName());
        foodie.setSurname(foodieDTO.getSurname());
        foodie.setBirthdate(foodieDTO.getBirthdate());
        foodie.setRegistrationDate(new Date());

        return foodie;
    }


    public RegistedUserInfoDTO entityToFoodieDTO (Foodie foodie) {

        return new RegistedUserInfoDTO(
                foodie.getUsername(),
                foodie.getName(),
                foodie.getSurname(),
                foodie.getEmail(),
                foodie.getBirthdate()
        );
    }


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
