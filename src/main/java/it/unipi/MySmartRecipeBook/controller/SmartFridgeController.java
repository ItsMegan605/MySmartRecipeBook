package it.unipi.MySmartRecipeBook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.MySmartRecipeBook.dto.IngredientsListDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.RecipeSuggestionDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.ShowRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.SliceRecipeDTO;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import it.unipi.MySmartRecipeBook.service.SmartFridgeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for the smart Fridge function
 */
@RestController
@RequestMapping("/api/fridge")
@Tag(name = "Smart Fridge", description = "Endpoints for managing the smart fridge and getting recipe recommendations")
public class SmartFridgeController {


    private final SmartFridgeService smartFridgeService;

    public SmartFridgeController(SmartFridgeService smartFridgeService) {
        this.smartFridgeService = smartFridgeService;
    }

    /**
     * Method to get the smart fridge and its content
     * @see SmartFridgeService#getSmartFridge() 
     * @return the Smart fridge contents
     */
    @GetMapping("/get")
    @Operation(summary = "Get the smart fridge")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<IngredientsListDTO> getList() {

        IngredientsListDTO ingredientsListDTO = smartFridgeService.getSmartFridge();
        return ResponseEntity.ok(ingredientsListDTO);
    }
    
    /**
     * Post Method to add ingredients to the smart fridge
     * @see SmartFridgeService#addIngredients(List) 
     * @return the Smart fridge and the new added ingredients
     */
    @PostMapping("/add")
    @Operation(summary = "Add ingredients to the fridge")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<?> addIngredient(@RequestBody List<String> ingredients) {
        IngredientsListDTO ingredientsListDTO = smartFridgeService.addIngredients(ingredients);
        return ResponseEntity.ok().body(ingredientsListDTO);
    }

    /**
     * Post Method to remove ingredients from the smart fridge 
     * @see SmartFridgeService#removeIngredient(String) 
     * @return returns the Smart fridge without the removed ingredients.
     */
    @DeleteMapping("/remove")
    @Operation(summary = "Remove ingredients from the fridge")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<?> removeIngredient(@RequestBody String ingredient ) {
        IngredientsListDTO ingredientsListDTO = smartFridgeService.removeIngredient(ingredient);
        return ResponseEntity.ok(ingredientsListDTO);
    }

    /**
     * Get Method to get recommendations when we add ingredients, and we want a recipe suggestion
     * @see SmartFridgeService#getRecommendations(String, int)
     * @return the Smart fridge's recipes suggestions
     */
    @GetMapping("/recommendations/{pageNum}")
    @Operation(summary = "Get recipe recommendations based on fridge contents")
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "204")
    })
    public ResponseEntity<SliceRecipeDTO<RecipeSuggestionDTO>> getRecommendations(@PathVariable int pageNum) {
        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        SliceRecipeDTO<RecipeSuggestionDTO> recipes = smartFridgeService.getRecommendations(authFoodie.getUsername(), pageNum);

        if (recipes.getContent().isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(recipes);
    }

    /**
     * Get method to retrieve a recipe's information from the fridge
     * @param id identifier of the requested recipe
     * @see SmartFridgeService#getFridgeRecipeById(String)
     * @return a ResponseEntity. Ok with the recipe details
     */
    @GetMapping("/recipe/{id}")
    @Operation(summary = "Get recipe details from the fridge")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<ShowRecipeDTO> getRecipe(@PathVariable String id){
        ShowRecipeDTO recipe = smartFridgeService.getFridgeRecipeById(id);
        return ResponseEntity.ok(recipe);
    }

}

