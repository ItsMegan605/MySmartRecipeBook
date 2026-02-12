package it.unipi.MySmartRecipeBook.controller.RedisController;

import it.unipi.MySmartRecipeBook.model.Neo4j.RecipeNeo4j;
import it.unipi.MySmartRecipeBook.model.Redis.SmartFridge;
import it.unipi.MySmartRecipeBook.repository.RecipeNeo4jRepository;
import it.unipi.MySmartRecipeBook.service.RecipeMatchService;
import it.unipi.MySmartRecipeBook.service.SmartFridgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> addIngredient(@PathVariable String userId, @RequestBody String ingredient) {
        try {
            SmartFridge list = smartFridgeService.addItem(userId, ingredient);
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeIngredient(@PathVariable String userId, @RequestBody String ingredient) {
        try {
            SmartFridge list = smartFridgeService.removeItem(userId, ingredient);
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<SmartFridge> getList(@PathVariable String userId) {
        return ResponseEntity.ok(smartFridgeService.getSmartFridge(userId));
    }

   /* @PostMapping("/recommendations")
    public ResponseEntity<List<RecipeNeo4j>> getRecommendations(@RequestBody List<String> ingredients) {
        // Chiamata al metodo d'istanza del service (non statico)
        List<RecipeNeo4j> recipes = RecipeMatchService.getSmartFridgeSuggestions(ingredients);

        if (recipes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(recipes);
    }*/

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