package it.unipi.MySmartRecipeBook.controller.RedisController;
/*
import it.unipi.MySmartRecipeBook.model.Redis.ShoppingList;
import it.unipi.MySmartRecipeBook.service.ShoppingListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/shopping")
public class ShoppingListController {

    @Autowired
    private ShoppingListService shoppingListService;

    //qui avevamo la ridondanza perchè faceva lui le operaizoni per niente
    //le deve fare il service quindi ho messo la chiamata diretta

    @PostMapping("/add")
    public ResponseEntity<?> addItem(@RequestBody String item) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            ShoppingList list = shoppingListService.addIngredient(username, item);
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeItem(@RequestBody String ingredient) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            ShoppingList list = shoppingListService.removeIngredient(username, ingredient);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<ShoppingList> getList() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(shoppingListService.getShoppingList(username));
    }
}
*/