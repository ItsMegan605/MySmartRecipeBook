package it.unipi.MySmartRecipeBook.controller;

import it.unipi.MySmartRecipeBook.model.ShoppingList;
import it.unipi.MySmartRecipeBook.service.ShoppingListService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//TO-DO: capire come vogliamo mettere gli ingredienti

@RestController
@RequestMapping("/api/shopping-list") // Tutte le chiamate iniziano con questo prefisso
public class ShoppingListController {

    private final ShoppingListService service;

    public ShoppingListController(ShoppingListService service) {
        this.service = service;
    }

    // GET: Vedi la lista
    // Esempio: GET localhost:8080/api/shopping-list/101
    @GetMapping("/{userId}")
    public ResponseEntity<ShoppingList> getList(@PathVariable Integer userId) {
        ShoppingList list = service.getList(userId);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/{userId}/add")
    public ResponseEntity<ShoppingList> add(
            @PathVariable Integer userId,
            @RequestBody String ingredient) {

        ShoppingList updatedList = service.addToShoppingList(userId, ingredient);
        return ResponseEntity.ok(updatedList);
    }

    @DeleteMapping("/{userId}/remove")
    public ResponseEntity<ShoppingList> remove(
            @PathVariable Integer userId,
            @RequestParam String ingredient) {

        ShoppingList updatedList = service.removeFromShoppingList(userId, ingredient);
        return ResponseEntity.ok(updatedList);
    }
}
