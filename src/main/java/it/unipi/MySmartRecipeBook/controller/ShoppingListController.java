package it.unipi.MySmartRecipeBook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.MySmartRecipeBook.dto.IngredientsListDTO;
import it.unipi.MySmartRecipeBook.service.ShoppingListService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Rest Controller for the Shopping List
 * General Endpoint: /api/shopping
 */
@RestController
@RequestMapping("/api/shopping")
@Tag(name = "Shopping List", description = "Endpoints for managing the user's shopping list")
public class ShoppingListController {

    private ShoppingListService shoppingListService;

    public ShoppingListController(ShoppingListService shoppingListService){
        this.shoppingListService = shoppingListService;
    }

    /**
     * Get Method to get the shopping list as foodie
     * @see ShoppingListService#getShoppingList()
     * @return The shopping list and its contents
     */
    @GetMapping("/get")
    @Operation(summary = "Get shopping list", description = "Retrieves the current user's shopping list.")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<IngredientsListDTO> getList() {

        IngredientsListDTO ingredientsListDTO = shoppingListService.getShoppingList();
        return ResponseEntity.ok(ingredientsListDTO);
    }

    /**
     * Post method to add items to the shopping list
     * @param items: items given by a user as a list of strings, more than one item
     *             can be added in one time
     * @see ShoppingListService#addIngredients(List)
     * @return The result of the shopping list with the new items
     */
    @PostMapping("/add")
    @Operation(summary = "Add ingredients", description = "Adds ingredients to the shopping list.")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<?> addItems(@RequestBody List<String> items) {

        IngredientsListDTO list = shoppingListService.addIngredients(items);
        return ResponseEntity.ok(list);
    }

    /**
     * Method to remove an item from the shopping list.
     * @param ingredient Give a single ingredient as a string to remove it
     * @return The result of the shopping list without the removed items
     * @see ShoppingListService#removeIngredient(String)
     */

    @PostMapping("/remove")
    @Operation(summary = "Remove ingredients", description = "Removes ingredients to the shopping list.")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<IngredientsListDTO> removeItem(@RequestBody String ingredient) {

        IngredientsListDTO list = shoppingListService.removeIngredient(ingredient);
        return ResponseEntity.ok(list);
    }

}
