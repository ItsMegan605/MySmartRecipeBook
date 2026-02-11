package it.unipi.MySmartRecipeBook.service;
import it.unipi.MySmartRecipeBook.dto.recipe.RecipeMatchDTO;
import it.unipi.MySmartRecipeBook.model.Neo4j.RecipeNeo4j;
import it.unipi.MySmartRecipeBook.repository.RecipeNeo4jRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecipeMatchService {

    private final RecipeNeo4jRepository recipeNeo4jRepository;

    @Autowired
    public RecipeMatchService(RecipeNeo4jRepository recipeNeo4jRepository) {
        this.recipeNeo4jRepository = recipeNeo4jRepository;
    }

    public List<RecipeNeo4j> getSmartFridgeSuggestions(List<String> ingredients) {
        return recipeNeo4jRepository.findRecipesByIngredients(ingredients);
    }
}
