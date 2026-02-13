package it.unipi.MySmartRecipeBook.controller.RedisController;

import it.unipi.MySmartRecipeBook.dto.recipe.RecipeSuggestionDTO;
import it.unipi.MySmartRecipeBook.model.Neo4j.RecipeNeo4j;
import it.unipi.MySmartRecipeBook.model.Redis.SmartFridge;
import it.unipi.MySmartRecipeBook.repository.RecipeNeo4jRepository;
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

    @Autowired
    private SmartFridgeService smartFridgeService;

    @Autowired
    private RecipeNeo4jRepository recipeNeo4jRepository;

    @PostMapping("/add")
    public ResponseEntity<?> addIngredient(@RequestBody String ingredient) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            SmartFridge list = smartFridgeService.addItem(username, ingredient);
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeIngredient(@RequestBody String ingredient) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            SmartFridge list = smartFridgeService.removeItem(username, ingredient);
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<SmartFridge> getList() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(smartFridgeService.getSmartFridge(username));
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

    /*
    //non so se serve intanto lo metto lì va aggiustato con Neo4j
    @GetMapping("/findRecipe")
    public  ResponseEntity<SmartFridge> getRecipe(@PathVariable String recipeId){
        return ResponseEntity.ok(smartFridgeService.getRecipeById);
    }
 */
    //lista di ricette salvata su redis
    //tolgo un ingrediente e quando lo tolgo devo controllare nuovamente le ricette

    //vedere se aggiungere metodo per neo4j
}