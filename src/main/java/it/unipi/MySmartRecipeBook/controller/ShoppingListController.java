package it.unipi.MySmartRecipeBook.controller;

import it.unipi.MySmartRecipeBook.model.Redis.ShoppingList;
import it.unipi.MySmartRecipeBook.service.ShoppingListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//TO-DO: capire come vogliamo mettere gli ingredienti

@RestController
@RequestMapping("/api/shopping")
public class ShoppingListController {

    @Autowired
    private ShoppingListService shoppingListService;

    @PostMapping("/add/{userId}")
    public ResponseEntity<String> addItem(@PathVariable Integer userId, @RequestBody String item) {
        ShoppingList list = shoppingListService.getShoppingList(userId);
        list.addItem(item);
        shoppingListService.saveShoppingList(list);
        return ResponseEntity.ok("Elemento aggiunto alla lista di " + userId);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ShoppingList> getList(@PathVariable Integer userId) {
        return ResponseEntity.ok(shoppingListService.getShoppingList(userId));
    }
}
