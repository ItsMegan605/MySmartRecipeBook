package it.unipi.MySmartRecipeBook.controller;

import it.unipi.MySmartRecipeBook.model.ShoppingList;
import it.unipi.MySmartRecipeBook.service.ShoppingListService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        ShoppingList list = service.getCart(userId);
        return ResponseEntity.ok(list);
    }

    // POST: Aggiungi un ingrediente
    // Esempio: POST localhost:8080/api/shopping-list/101/add
    // Body (Raw Text): "Latte"
    @PostMapping("/{userId}/add")
    public ResponseEntity<ShoppingList> add(
            @PathVariable Integer userId,
            @RequestBody String ingredient) {

        ShoppingList updatedList = service.addToCart(userId, ingredient);
        return ResponseEntity.ok(updatedList);
    }

    // DELETE: Rimuovi un ingrediente
    // Esempio: DELETE localhost:8080/api/shopping-list/101/remove?ingredient=Latte
    // Nota: Qui usiamo un parametro "?ingredient=..." che è più standard per le DELETE
    @DeleteMapping("/{userId}/remove")
    public ResponseEntity<ShoppingList> remove(
            @PathVariable Integer userId,
            @RequestParam String ingredient) {

        ShoppingList updatedList = service.removeFromCart(userId, ingredient);
        return ResponseEntity.ok(updatedList);
    }
}