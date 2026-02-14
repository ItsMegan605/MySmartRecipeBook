package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.foodie.StandardFoodieDTO;
import it.unipi.MySmartRecipeBook.dto.foodie.UpdateStandardFoodieDTO;
import it.unipi.MySmartRecipeBook.model.Chef;
import it.unipi.MySmartRecipeBook.model.Foodie;
import it.unipi.MySmartRecipeBook.model.Mongo.*;
import it.unipi.MySmartRecipeBook.repository.ChefRepository;
import it.unipi.MySmartRecipeBook.repository.FoodieRepository;
import it.unipi.MySmartRecipeBook.repository.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.utils.UsersConvertions;

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

    /* PROFILE MANAGEMENT */

    public StandardFoodieDTO getByUsername(String username) {

        Foodie foodie = foodieRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        return usersConvertions.entityToFoodieDTO(foodie);
    }

    public StandardFoodieDTO updateFoodie(String username, UpdateStandardFoodieDTO dto) {

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

    public void deleteFoodie(String username){

        Foodie foodie = foodieRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        foodieRepository.delete(foodie);
    }


    public void saveRecipe(String foodieId, String recipeId) {

        Foodie foodie = foodieRepository.findById(foodieId)
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        //check if the recipe has already been saved in the old recipes
        for (FoodieRecipeSummary recipe : foodie.getOldSavedRecipes()){
            if (recipe.getId().equals(recipeId))
                throw new RuntimeException("Recipe already saved");
        }

        //check if the recipe has already been saved in the last recipes
        for (FoodieRecipe recipe : foodie.getNewSavedRecipes()) {
            if (recipe.getId().equals(recipeId))
                throw new RuntimeException("Recipe already saved");
        }

        RecipeMongo recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe to save not found"));


        FoodieRecipe fullRecipe = usersConvertions.entityToFoodieEntity(recipe);

        if (foodie.getNewSavedRecipes() == null) {
            foodie.setNewSavedRecipes(new ArrayList<>());
        }

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

        String chefId = fullRecipe.getChef().getMongoId();
        updateChefCounters(chefId, recipeId);
    }

    /* Attenzione perchè non so se questa operazione è atomica, quindi nel caso bisogna gestire se viene salvata
    la ricetta ma non aggiornati i contatori */
    private void updateChefCounters(String chefId, String recipeId){

        Chef chef = chefRepository.findById(chefId)
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        boolean recipeFound = false;
        for (ChefRecipe recipe : chef.getNewRecipes()) {
            if (recipe.getId().equals(recipeId)){
                recipe.setNumSaves(recipe.getNumSaves() + 1);
                recipeFound = true;
                break;
            }
        }

        if (!recipeFound){
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


    public void removeSavedRecipe(String username, String recipeId) {

        Foodie foodie = foodieRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        /* Da modificare considerando che la ricerca va fatta in due liste
        foodie.getSavedRecipeIds().remove(recipeId);
        foodieRepository.save(foodie);*/
    }

}
