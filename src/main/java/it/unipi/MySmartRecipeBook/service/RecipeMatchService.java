package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.recipe.RecipeSuggestionDTO; // Usa il DTO se segui il pattern My-Akiba
import it.unipi.MySmartRecipeBook.repository.Neo4j.RecipeNeo4jRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 */
@Service
public class RecipeMatchService {

    private final RecipeNeo4jRepository recipeNeo4jRepository;

    /**
     *
     * @param recipeNeo4jRepository
     */
    @Autowired
    public RecipeMatchService(RecipeNeo4jRepository recipeNeo4jRepository) {
        this.recipeNeo4jRepository = recipeNeo4jRepository;
    }

    /**
     *
     * @param ingredients
     * @return
     */
    public List<RecipeSuggestionDTO> getSmartFridgeSuggestions(List<String> ingredients) {
        return recipeNeo4jRepository.findRecipesByIngredients(ingredients);
    }
}
