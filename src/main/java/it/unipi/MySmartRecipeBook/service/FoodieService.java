package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.foodie.StandardFoodieDTO;
import it.unipi.MySmartRecipeBook.dto.foodie.UpdateFoodieDTO;
import it.unipi.MySmartRecipeBook.model.Chef;
import it.unipi.MySmartRecipeBook.model.Foodie;
import it.unipi.MySmartRecipeBook.model.Mongo.*;
import it.unipi.MySmartRecipeBook.repository.ChefRepository;
import it.unipi.MySmartRecipeBook.repository.FoodieRepository;
import it.unipi.MySmartRecipeBook.repository.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.utils.UsersConvertions;

import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class FoodieService {

    private final FoodieRepository foodieRepository;
    private final ChefRepository chefRepository;
    private final RecipeMongoRepository recipeRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsersConvertions usersConvertions;

    public FoodieService(FoodieRepository foodieRepository, RecipeMongoRepository recipeRepository,
                         PasswordEncoder passwordEncoder, UsersConvertions usersConvertions,
                         ChefRepository chefRepository) {
        this.foodieRepository = foodieRepository;
        this.recipeRepository = recipeRepository;
        this.passwordEncoder = passwordEncoder;
        this.usersConvertions = usersConvertions;
        this.chefRepository = chefRepository;
    }


    /*--------------- Retrieve foodie's informations ----------------*/

    public StandardFoodieDTO getByUsername(String username) {

        Foodie foodie = foodieRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        return usersConvertions.entityToFoodieDTO(foodie);
    }


    /*--------------- Change foodie's informations ----------------*/
    /* This function allows a foodie to change its personal information, in particolar one or more among the following
    fields:
        - Name
        - Surname
        - Email
        - Password
        - Birthday

     We don't allow a foodie to change his/her username for security reasons */

    public StandardFoodieDTO updateFoodie(String username, UpdateFoodieDTO dto) {

        Foodie foodie = foodieRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        if (dto.getName() != null)
            foodie.setName(dto.getName());

        if (dto.getSurname() != null)
            foodie.setSurname(dto.getSurname());

        if (dto.getEmail() != null)
            foodie.setEmail(dto.getEmail());

        /* Supponiamo che se si è loggato può cambiare la password senza fare ulteriori controlli? */
        if (dto.getPassword() != null && !dto.getPassword().isBlank())
            foodie.setPassword(passwordEncoder.encode(dto.getPassword()));

        if (dto.getBirthdate() != null)
            foodie.setBirthdate(dto.getBirthdate());

        foodieRepository.save(foodie);
        return usersConvertions.entityToFoodieDTO(foodie);
    }


    /*----------------- Delete foodie's Profile ------------------*/

    public void deleteFoodie(String username){

        Foodie foodie = foodieRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        foodieRepository.delete(foodie);
    }


    /*------------ Add a recipe to foodie's favourites  -------------*/

    public void saveRecipe(String foodieId, String recipeId) {

        Foodie foodie = foodieRepository.findById(foodieId)
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        if(foodie.getNewSavedRecipes() != null) {
            //check if the recipe has already been saved in the last recipes
            for (FoodieRecipe recipe : foodie.getNewSavedRecipes()) {
                if (recipe.getId().equals(recipeId))
                    throw new RuntimeException("Recipe already saved");
            }
        }

        if(foodie.getOldSavedRecipes() != null) {
            //check if the recipe has already been saved in the old recipes
            for (FoodieRecipeSummary recipe : foodie.getOldSavedRecipes()) {
                if (recipe.getId().equals(recipeId))
                    throw new RuntimeException("Recipe already saved");
            }
        }

        /* We retrieve all the recipe informations from the recipe collection*/
        RecipeMongo recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe to save not found"));

        FoodieRecipe fullRecipe = usersConvertions.entityToFoodieEntity(recipe);

        /* If it is the first save, we have to create the NewSavedRecipes list */
        if (foodie.getNewSavedRecipes() == null) {
            foodie.setNewSavedRecipes(new ArrayList<>());
        }

        /* Il the NewSavedRecipes list is already full we have to move the oldest recipe of the list in the OldSavedRecipe
        list and then we insert the new recipe in the NewSavedRecipe */
        if(foodie.getNewSavedRecipes().size() == 5){

            if (foodie.getOldSavedRecipes() == null) {
                foodie.setOldSavedRecipes(new ArrayList<>());
            }

            FoodieRecipe oldestRecipe = foodie.getNewSavedRecipes().remove(0);
            FoodieRecipeSummary reduced_old = usersConvertions.entityToReducedRecipe(oldestRecipe);
            foodie.getOldSavedRecipes().add(reduced_old);
        }

        foodie.getNewSavedRecipes().add(fullRecipe);
        foodieRepository.save(foodie);

        /* We have to increment two different counters, both in the chef collection:
            - the one that keep trace of the number of saves for the specific recipe
            - the global counter that keep trace of the total number of saves for a specific chef */
        String chefId = fullRecipe.getChef().getMongoId();
        updateChefCounters(chefId, recipeId);
    }


    // @Transactional
    private void updateChefCounters(String chefId, String recipeId){

        Chef chef = chefRepository.findById(chefId)
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        boolean recipeFound = false;
        if(chef.getNewRecipes() == null){
            throw new RuntimeException("Recipe not found for the specified chef");
        }

        for (ChefRecipe recipe : chef.getNewRecipes()) {
            if (recipe.getId().equals(recipeId)){
                recipe.setNumSaves(recipe.getNumSaves() + 1);
                recipeFound = true;
                break;
            }
        }

        if (!recipeFound && chef.getOldRecipes() != null){
            for (ChefRecipeSummary recipe : chef.getOldRecipes()) {
                if (recipe.getMongoId().equals(recipeId)){
                    recipe.setNumSaves(recipe.getNumSaves() + 1);
                    recipeFound = true;
                    break;
                }
            }
        }

        if(recipeFound){
            chef.setTotalSaves(chef.getTotalSaves() + 1);
            chefRepository.save(chef);
        }
    }


    public void removeSavedRecipe(String foodieId, String recipeId) {

        Foodie foodie = foodieRepository.findById(foodieId)
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        if(foodie.getNewSavedRecipes() ==  null){
            throw new RuntimeException("Recipe not found for the specified foodie");
        }

        FoodieRecipe recipeRemoved = null;
        for (FoodieRecipe recipe : foodie.getNewSavedRecipes()) {
            if(recipe.getId().equals(recipeId)){
                recipeRemoved = recipe;
                foodie.getNewSavedRecipes().remove(recipe);

                if(foodie.getOldSavedRecipes() != null){

                    String oldRecipeId = foodie.getOldSavedRecipes().get(0).getId();
                    RecipeMongo oldRecipe =  recipeRepository.findById(oldRecipeId)
                            .orElseThrow(() -> new RuntimeException("Recipe not found"));

                    FoodieRecipe recipeToMove = usersConvertions.entityToFoodieEntity(oldRecipe);
                    foodie.getNewSavedRecipes().add(recipeToMove);

                    foodie.getOldSavedRecipes().remove(foodie.getOldSavedRecipes().get(0));
                }
                foodieRepository.save(foodie);
                break;
            }
        }

        if(recipeRemoved == null &&  foodie.getOldSavedRecipes() != null){
            for (FoodieRecipeSummary recipe : foodie.getOldSavedRecipes()) {
                if (recipe.getId().equals(recipeId)){
                    foodie.getOldSavedRecipes().remove(recipe);
                    foodieRepository.save(foodie);
                    break;
                }
            }
        }

        /* Il fatto che devono essere decrementati i counter si può fare in un secondo momento? */

    }

}
