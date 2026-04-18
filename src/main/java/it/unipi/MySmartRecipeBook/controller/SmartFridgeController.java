package it.unipi.MySmartRecipeBook.controller;

import it.unipi.MySmartRecipeBook.dto.IngredientsListDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.RecipeSuggestionDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.ShowRecipeDTO;
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
public class SmartFridgeController {


    private SmartFridgeService smartFridgeService;

    public SmartFridgeController(SmartFridgeService smartFridgeService) {
        this.smartFridgeService = smartFridgeService;
    }

    /**
     * Method to get the smart fridge and its content
     * @see SmartFridgeService#getSmartFridge() 
     * @return the Smart fridge contents
     */
    @GetMapping("/get")
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
    public ResponseEntity<?> addIngredient(@RequestBody List<String> ingredients) {
        IngredientsListDTO ingredientsListDTO = smartFridgeService.addIngredients(ingredients);
        return ResponseEntity.ok().body(ingredientsListDTO);
    }

    /**
     * Post Method to remove ingredients from the smart fridge 
     * @see SmartFridgeService#removeIngredient(String) 
     * @return returns the Smart fridge without the removed ingredients.
     */

    @PostMapping("/remove")
    public ResponseEntity<?> removeIngredient(@RequestBody String ingredient ) {
        IngredientsListDTO ingredientsListDTO = smartFridgeService.removeIngredient(ingredient);
        return ResponseEntity.ok(ingredientsListDTO);
    }

    /**
     * Get Method to get recommendations when we add ingredients and we want a recipe suggestion
     * @see SmartFridgeService#getRecommendations(String)
     * @return the Smart fridge's recipes suggestions
     */
    @GetMapping("/recommendations")
    public ResponseEntity<List<RecipeSuggestionDTO>> getRecommendations() {
        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        List<RecipeSuggestionDTO> recipes = smartFridgeService.getRecommendations(authFoodie.getUsername());

        if (recipes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(recipes);
    }

    /**
     * Get method to retrieve a recipe's information from the fridge
     * @param id identifier of the requested recipe
     * @see SmartFridgeService#getFridgeRecipeById(String)
     * @return a ResponseEntity.ok with the recipe details
     */

    @GetMapping("/recipe/{id}")
    public ResponseEntity<ShowRecipeDTO> getRecipe(@PathVariable String id){
        ShowRecipeDTO recipe = smartFridgeService.getFridgeRecipeById(id);
        return ResponseEntity.ok(recipe);
    }

}

