package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.foodie.FoodieDTO;
import it.unipi.MySmartRecipeBook.dto.foodie.UpdateFoodieDTO;
import it.unipi.MySmartRecipeBook.model.Foodie;
import it.unipi.MySmartRecipeBook.model.Mongo.FoodieRecipeMongo;
import it.unipi.MySmartRecipeBook.model.Mongo.RecipeMongo;
import it.unipi.MySmartRecipeBook.model.Mongo.ReducedRecipeMongo;
import it.unipi.MySmartRecipeBook.repository.FoodieRepository;
import it.unipi.MySmartRecipeBook.repository.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.utils.UsersConvertions;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class FoodieService {

    private final FoodieRepository foodieRepository;
    private final RecipeMongoRepository recipeRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsersConvertions usersConvertions;

    public FoodieService(FoodieRepository foodieRepository,
                         RecipeMongoRepository recipeRepository,
                         PasswordEncoder passwordEncoder,
                         UsersConvertions usersConvertions) {
        this.foodieRepository = foodieRepository;
        this.recipeRepository = recipeRepository;
        this.passwordEncoder = passwordEncoder;
        this.usersConvertions = usersConvertions;
    }

    /* PROFILE MANAGEMENT */

    public FoodieDTO getByUsername(String username) {

        Foodie foodie = foodieRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        return usersConvertions.entityToFoodieDTO(foodie);
    }

    public FoodieDTO updateFoodie(String username, UpdateFoodieDTO dto) {

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

    public void deleteFoodie(String username) {

        Foodie foodie = foodieRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        foodieRepository.delete(foodie);
    }

    /* =========================
       SAVED RECIPES
       ========================= */

    public void saveRecipe(String username, String recipeId) {

        Foodie foodie = foodieRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        /* We check if the recipe has already been saved in the old recipes */
        for (ReducedRecipeMongo recipe : foodie.getSavedRecipes()){
            if (recipe.getId().equals(recipeId))
                throw new RuntimeException("Recipe already saved");
        }

        /* We check if the recipe has already been saved in the last recipes */
        for (FoodieRecipeMongo recipe : foodie.getLastSavedRecipes()) {
            if (recipe.getId().equals(recipeId))
                throw new RuntimeException("Recipe already saved");
        }

        RecipeMongo recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe to save not found"));


        FoodieRecipeMongo fullRecipe = usersConvertions.entityToFoodieEntity(recipe);

        if(foodie.getLastSavedRecipes().size() == 5){

            FoodieRecipeMongo oldestRecipe = foodie.getLastSavedRecipes().get(0);
            ReducedRecipeMongo reduced_old = usersConvertions.entityToReducedRecipe(oldestRecipe);
            foodie.getSavedRecipes().add(reduced_old);
            foodie.getLastSavedRecipes().remove(reduced_old);
        }

        foodie.getLastSavedRecipes().add(fullRecipe);
        foodieRepository.save(foodie);

        String chefName = fullRecipe.getChefName();
        updateChefCounters(chefName, recipeId);
        /* Bisogna prendere il nome dello chef, cercare nella collection chef e modificare il counter dei saves
        ricercandolo in entrambe le liste ed eventualmente decrementando il contatore globale */
    }

    private void updateChefCounters(String chefName, String recipeId) {

    }


    public void removeSavedRecipe(String username, String recipeId) {

        Foodie foodie = foodieRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        /* Da modificare considerando che la ricerca va fatta in due liste
        foodie.getSavedRecipeIds().remove(recipeId);
        foodieRepository.save(foodie);*/
    }



}
