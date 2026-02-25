package it.unipi.MySmartRecipeBook.controller.RedisController;

import it.unipi.MySmartRecipeBook.dto.IngredientsListDTO;
import it.unipi.MySmartRecipeBook.model.Redis.ShoppingList;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import it.unipi.MySmartRecipeBook.service.ShoppingListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/shopping")
public class ShoppingListController {

    private ShoppingListService shoppingListService;

    public ShoppingListController(ShoppingListService shoppingListService){
        this.shoppingListService = shoppingListService;
    }

    @GetMapping("/get")
    public ResponseEntity<IngredientsListDTO> getList() {

        IngredientsListDTO ingredientsListDTO = shoppingListService.getShoppingList();
        return ResponseEntity.ok(ingredientsListDTO);
    }


    @PostMapping("/add")
    public ResponseEntity<?> addItems(@RequestBody List<String> items) {

        IngredientsListDTO list = shoppingListService.addIngredients(items);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/remove")
    public ResponseEntity<IngredientsListDTO> removeItem(@RequestBody String ingredient) {

        IngredientsListDTO list = shoppingListService.removeIngredient(ingredient);
        return ResponseEntity.ok(list);
    }

}
