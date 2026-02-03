package it.unipi.MySmartRecipeBook.controller;

import it.unipi.MySmartRecipeBook.model.Mongo.RecipeMongo;
import it.unipi.MySmartRecipeBook.service.SmartFridgeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/fridge")
public class SmartFridgeController {

    private final SmartFridgeService fridgeService;

    public SmartFridgeController(SmartFridgeService fridgeService) {
        this.fridgeService = fridgeService;
    }

    @PostMapping("/{userId}/add")
    public ResponseEntity<Void> addIngredient(
            @PathVariable Integer userId, // Cambiato in Integer
            @RequestBody String ingredient) {
        fridgeService.addIngredientToFridge(userId, ingredient);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/recipes") // Endpoint mancante per vedere i risultati
    public ResponseEntity<List<RecipeMongo>> getPossibleRecipes(@PathVariable Integer userId) {
        List<RecipeMongo> recipes = fridgeService.whatCanICook(userId);
        return ResponseEntity.ok(recipes);
    }
}