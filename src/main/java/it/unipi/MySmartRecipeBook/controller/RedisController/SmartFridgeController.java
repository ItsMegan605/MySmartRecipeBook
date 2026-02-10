package it.unipi.MySmartRecipeBook.controller.RedisController;

import it.unipi.MySmartRecipeBook.model.Redis.SmartFridge;
import it.unipi.MySmartRecipeBook.service.SmartFridgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fridge")
public class SmartFridgeController {

    @Autowired
    private SmartFridgeService smartFridgeService;

    @PostMapping("/add/{userId}")
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

    @PostMapping("/remove/{userId}")
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

    @GetMapping("/{userId}")
    public ResponseEntity<SmartFridge> getList(@PathVariable String userId) {
        return ResponseEntity.ok(smartFridgeService.getSmartFridge(userId));
    }

    //vedere se aggiungere metodo per neo4j
}