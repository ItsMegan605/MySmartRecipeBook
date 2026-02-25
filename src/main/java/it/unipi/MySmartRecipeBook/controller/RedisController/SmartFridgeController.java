package it.unipi.MySmartRecipeBook.controller.RedisController;

import it.unipi.MySmartRecipeBook.dto.IngredientsListDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.RecipeSuggestionDTO;
import it.unipi.MySmartRecipeBook.model.Neo4j.RecipeNeo4j;
import it.unipi.MySmartRecipeBook.model.Redis.SmartFridge;
import it.unipi.MySmartRecipeBook.repository.RecipeNeo4jRepository;
import it.unipi.MySmartRecipeBook.service.IngredientService;
import it.unipi.MySmartRecipeBook.service.RecipeMatchService;
import it.unipi.MySmartRecipeBook.service.SmartFridgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import it.unipi.MySmartRecipeBook.model.*;

import java.util.List;

@RestController
@RequestMapping("/api/fridge")
public class SmartFridgeController {


    private SmartFridgeService smartFridgeService;
    private RecipeNeo4jRepository recipeNeo4jRepository;

    //aggiunto costruttore
    public SmartFridgeController(SmartFridgeService smartFridgeService, RecipeNeo4jRepository recipeNeo4jRepository) {
        this.smartFridgeService = smartFridgeService;
        this.recipeNeo4jRepository = recipeNeo4jRepository;
    }

    @GetMapping("/get")
    public ResponseEntity<IngredientsListDTO> getList() {

        IngredientsListDTO ingredientsListDTO = smartFridgeService.getSmartFridge();
        return ResponseEntity.ok(ingredientsListDTO);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addIngredient(@RequestBody List<String> ingredients) {
        IngredientsListDTO ingredientsListDTO = smartFridgeService.addIngredients(ingredients);
        return ResponseEntity.ok().body(ingredientsListDTO);
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeIngredient(@RequestBody String ingredient ) {
        IngredientsListDTO ingredientsListDTO = smartFridgeService.removeIngredient(ingredient);
        return ResponseEntity.ok(ingredientsListDTO);
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<RecipeSuggestionDTO>> getRecommendations() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<RecipeSuggestionDTO> recipes = smartFridgeService.getRecommendations(username);

        if (recipes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(recipes);
    }

}
/*
    //non so se serve intanto lo metto lì va aggiustato con Neo4j
    @GetMapping("/findRecipe")
    public  ResponseEntity<SmartFridge> getRecipe(@PathVariable String recipeId){
        return ResponseEntity.ok(smartFridgeService.getRecipeById());
    }


    //lista di ricette salvata su redis
    //tolgo un ingrediente e quando lo tolgo devo controllare nuovamente le ricette

    //vedere se aggiungere metodo per neo4j */
