package it.unipi.MySmartRecipeBook.controller.RedisController;

import it.unipi.MySmartRecipeBook.model.Redis.SmartFridge;
import it.unipi.MySmartRecipeBook.model.SmartFridgeIngredient;
import it.unipi.MySmartRecipeBook.service.SmartFridgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fridge")
public class SmartFridgeController {

    @Autowired
    private SmartFridgeService smartFridgeService;

    @PostMapping("/{userId}/add")
    public ResponseEntity<String> addIngredient(@PathVariable Integer userId, @RequestBody SmartFridgeIngredient ingredient) {
        SmartFridge fridge = smartFridgeService.getFridgeByUserId(userId);
        fridge.getIngredients().add(ingredient);
        smartFridgeService.updateFridge(fridge);
        return ResponseEntity.ok("Ingrediente aggiunto al frigo di " + userId);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<SmartFridge> getFridge(@PathVariable Integer userId) {
        return ResponseEntity.ok(smartFridgeService.getFridgeByUserId(userId));
    }
}