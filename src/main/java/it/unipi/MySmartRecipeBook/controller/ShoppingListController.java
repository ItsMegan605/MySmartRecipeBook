package it.unipi.MySmartRecipeBook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.MySmartRecipeBook.dto.IngredientSuggestionDTO;
import it.unipi.MySmartRecipeBook.dto.IngredientsListDTO;
import it.unipi.MySmartRecipeBook.service.ShoppingListService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing the user's shopping list.
 */
@RestController
@RequestMapping("/api/shopping")
@Tag(name = "Shopping List", description = "Endpoints for managing the user's shopping list")
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    public ShoppingListController(ShoppingListService shoppingListService){
        this.shoppingListService = shoppingListService;
    }


    /**
     * Retrieves the shopping list of the currently authenticated user.
     * @return a {@link ResponseEntity} containing an {@link IngredientsListDTO} with the shopping list and its contents
     * @see ShoppingListService#getShoppingList()
     */
    @GetMapping("/get")
    @Operation(summary = "Retrieve shopping list", description = "Fetches the currently authenticated user's shopping list and its contents.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Shopping list successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Foodie not found")
    })
    public ResponseEntity<IngredientsListDTO> getList() {

        IngredientsListDTO ingredientsListDTO = shoppingListService.getShoppingList();
        return ResponseEntity.ok(ingredientsListDTO);
    }


    /**
     * Adds one or more ingredients to the user's shopping list.
     * @param items a {@link List} of strings representing the ingredients to be added
     * @return a {@link ResponseEntity} containing an {@link IngredientsListDTO} with the updated shopping list
     * @see ShoppingListService#addIngredients(List)
     */
    @PostMapping("/add")
    @Operation(summary = "Add ingredients", description = "Adds a list of provided ingredients to the user's shopping list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ingredients successfully added"),
            @ApiResponse(responseCode = "400", description = "Invalid ingredient list provided"),
            @ApiResponse(responseCode = "404", description = "Foodie not found")
    })
    public ResponseEntity<IngredientsListDTO> addItems(@RequestBody List<String> items) {

        IngredientsListDTO list = shoppingListService.addIngredients(items);
        return ResponseEntity.ok(list);
    }


    /**
     * Removes a specific ingredient from the user's shopping list.
     * @param ingredient a {@link String} representing the single ingredient to remove
     * @return a {@link ResponseEntity} containing an {@link IngredientsListDTO} with the updated shopping list
     * @see ShoppingListService#removeIngredient(String)
     */
    @DeleteMapping("/remove")
    @Operation(summary = "Remove ingredient", description = "Removes a specific ingredient from the user's shopping list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ingredient successfully removed"),
            @ApiResponse(responseCode = "404", description = "Ingredient not found in the shopping list")
    })
    public ResponseEntity<IngredientsListDTO> removeItem(@RequestBody String ingredient) {

        IngredientsListDTO list = shoppingListService.removeIngredient(ingredient);
        return ResponseEntity.ok(list);
    }


    /**
     * Retrieves a list of suggested similar or complementary ingredients.
     * @return a {@link ResponseEntity} containing a {@link List} of {@link IngredientSuggestionDTO} representing the suggested ingredients
     * @see ShoppingListService#getSuggestedIngredients()
     */
    @GetMapping("/suggestedIngredients")
    @Operation(summary = "Suggest ingredients", description = "Retrieves a list of suggested ingredients based on the user's current shopping list or preferences.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Suggested ingredients successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Foodie not found")
    })
    public ResponseEntity<List<IngredientSuggestionDTO>> suggestIngredient() {
        List<IngredientSuggestionDTO> ingredientList = shoppingListService.getSuggestedIngredients();
        return ResponseEntity.ok(ingredientList);
    }
}
