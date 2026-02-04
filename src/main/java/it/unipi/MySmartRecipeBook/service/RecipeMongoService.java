package it.unipi.MySmartRecipeBook.service;

package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.CreateRecipeDTO;
import it.unipi.MySmartRecipeBook.model.RecipeMongo;
import it.unipi.MySmartRecipeBook.repository.RecipeMongoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecipeMongoService {

    private final RecipeMongoRepository recipeRepository;

    public RecipeMongoService(RecipeMongoRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    /* =========================
       CRUD OPERATIONS
       ========================= */

    public RecipeMongo createRecipe(CreateRecipeDTO dto) {

        RecipeMongo recipe = new RecipeMongo();
        recipe.setTitle(dto.getTitle());
        recipe.setCategory(dto.getCategory());
        recipe.setPreparation(dto.getPreparation());
        recipe.setPrepTime(dto.getPrepTime());
        recipe.setDifficulty(dto.getDifficulty());
        recipe.setDescription(dto.getDescription());
        recipe.setPhotoURL(dto.getPhotoURL());
        recipe.setChefUsername(dto.getChefUsername());
        recipe.setIngredients(dto.getIngredients());


        return recipeRepository.save(recipe);
    }

    public Optional<RecipeMongo> getRecipeById(String id) {
        return recipeRepository.findById(id);
    }

    public List<RecipeMongo> getAllRecipes() {
        return recipeRepository.findAll();
    }

    public void deleteRecipe(String recipeId) {
        recipeRepository.deleteById(recipeId);
    }
}
