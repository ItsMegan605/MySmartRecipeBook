package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.CreateRecipeDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.RecipeMongo;
import it.unipi.MySmartRecipeBook.model.Neo4j.RecipeNeo4j;
import it.unipi.MySmartRecipeBook.repository.RecipeMongoRepository;
import org.springframework.stereotype.Service;
import it.unipi.MySmartRecipeBook.dto.UpdateRecipeDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RecipeService {

    private final RecipeMongoRepository recipeRepository;

    public RecipeService(RecipeMongoRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    /* =========================
       CRUD OPERATIONS
       ========================= */

    private RecipeMongo createRecipeMongo(CreateRecipeDTO dto){

        RecipeMongo recipe = new RecipeMongo();
        recipe.setTitle(dto.getTitle());
        recipe.setCategory(dto.getCategory());
        recipe.setPreparation(dto.getPreparation());
        recipe.setPrepTime(dto.getPrepTime());
        recipe.setDifficulty(dto.getDifficulty());
        recipe.setDescription(dto.getDescription());
        recipe.setImageURL(dto.getImageURL());
        recipe.setChefUsername(dto.getChefUsername());
        recipe.setIngredients(dto.getIngredients());
        recipe.setCreationDate(LocalDateTime.now());

        return recipeRepository.save(recipe);
    }

    /*
    private RecipeNeo4j createRecipeNeo4j(CreateRecipeDTO dto){

    }*/

    public RecipeMongo createRecipe(CreateRecipeDTO dto) {

        RecipeMongo savedRecipe = createRecipeMongo(dto);
        //createRecipeNeo4j(dto);

        // per il momento facciamo solo il salvataggio in mongo, dovremmo poi gestire
        // anche il salvataggio in Neo4j più eventuali repliche
        return savedRecipe;
    }


    public RecipeMongo updateRecipe(String id, UpdateRecipeDTO dto) {
        RecipeMongo recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        if (dto.getTitle() != null) recipe.setTitle(dto.getTitle());
        if (dto.getDescription() != null) recipe.setDescription(dto.getDescription());
        if (dto.getIngredients() != null) recipe.setIngredients(dto.getIngredients());
        if (dto.getChefUsername() != null) recipe.setChefUsername(dto.getChefUsername());

        // IMMAGINE: scegli uno dei due (vedi sotto)
        if (dto.getImageURL() != null) recipe.setImageURL(dto.getImageURL()); // se nel model si chiama photoURL

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
