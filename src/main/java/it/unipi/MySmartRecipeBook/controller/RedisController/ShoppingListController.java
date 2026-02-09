package it.unipi.MySmartRecipeBook.controller.RedisController;

import it.unipi.MySmartRecipeBook.model.Redis.ShoppingList;
import it.unipi.MySmartRecipeBook.service.ShoppingListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/shopping")
public class ShoppingListController {

    @Autowired
    private ShoppingListService shoppingListService;

    //qui avevamo la ridondanza perchè faceva lui le operaizoni per niente
    //le deve fare il service quindi ho messo la chiamata diretta

    @PostMapping("/add/{userId}")
    public ResponseEntity<?> addItem(@PathVariable String userId, @RequestBody String item) {
        try {
            ShoppingList list = shoppingListService.addIngredient(userId, item);
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping("/remove/{userId}")
    public ResponseEntity<?> removeItem(@PathVariable String userId, @RequestBody String ingredient) {
        try {
            ShoppingList list = shoppingListService.removeIngredient(userId, ingredient);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ShoppingList> getList(@PathVariable String userId) {
        return ResponseEntity.ok(shoppingListService.getShoppingList(userId));
    }
}