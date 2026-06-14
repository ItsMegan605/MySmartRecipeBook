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
 * REST Controller for managing the user's Smart Fridge.
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
     * Retrieves the current user's smart fridge and its contents.
     * @return a {@link ResponseEntity} containing an {@link IngredientsListDTO} with the smart fridge contents
     * @see SmartFridgeService#getSmartFridge()
     */
    @GetMapping("/get")
    @Operation(summary = "Retrieve smart fridge", description = "Fetches the currently authenticated user's smart fridge and its ingredient contents.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Smart fridge successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Foodie not found")
    })
    public ResponseEntity<IngredientsListDTO> getList() {

        IngredientsListDTO ingredientsListDTO = smartFridgeService.getSmartFridge();
        return ResponseEntity.ok(ingredientsListDTO);
    }


    /**
     * Adds a list of ingredients to the user's smart fridge.
     * @param ingredients a {@link List} of strings representing the ingredients to be added
     * @return a {@link ResponseEntity} containing an {@link IngredientsListDTO} with the updated smart fridge contents
     * @see SmartFridgeService#addIngredients(List)
     */
    @PostMapping("/add")
    @Operation(summary = "Add ingredients", description = "Adds one or more ingredients to the user's smart fridge.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ingredients successfully added"),
            @ApiResponse(responseCode = "400", description = "Invalid ingredient list provided"),
            @ApiResponse(responseCode = "404", description = "Foodie not found")
    })
    public ResponseEntity<IngredientsListDTO> addIngredient(@RequestBody List<String> ingredients) {
        IngredientsListDTO ingredientsListDTO = smartFridgeService.addIngredients(ingredients);
        return ResponseEntity.ok().body(ingredientsListDTO);
    }


    /**
     * Removes a specific ingredient from the user's smart fridge.
     * @param ingredient a {@link String} representing the ingredient to remove
     * @return a {@link ResponseEntity} containing an {@link IngredientsListDTO} with the updated smart fridge contents
     * @see SmartFridgeService#removeIngredient(String)
     */
    @DeleteMapping("/remove")
    @Operation(summary = "Remove ingredient", description = "Removes a specific ingredient from the user's smart fridge.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ingredient successfully removed"),
            @ApiResponse(responseCode = "400", description = "Invalid ingredient provided"),
            @ApiResponse(responseCode = "404", description = "Ingredient not found in the fridge or Foodie not found")
    })
    public ResponseEntity<IngredientsListDTO> removeIngredient(@RequestBody String ingredient ) {
        IngredientsListDTO ingredientsListDTO = smartFridgeService.removeIngredient(ingredient);
        return ResponseEntity.ok(ingredientsListDTO);
    }


    /**
     * Retrieves a paginated list of recipe recommendations based on the current contents of the user's smart fridge.
     * @param pageNumber the requested page number
     * @return a {@link ResponseEntity} containing a list of {@link RecipeSuggestionDTO} with the recommended recipes, or no content if none are found
     * @see SmartFridgeService#getRecommendations(String, int)
     */
    @GetMapping("/recommendations/{pageNumber}")
    @Operation(summary = "Get recipe recommendations", description = "Retrieves a paginated list of suggested recipes that can be made using the ingredients currently stored in the smart fridge.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recommendations successfully retrieved"),
            @ApiResponse(responseCode = "204", description = "No recipe recommendations found for the current fridge contents"),
            @ApiResponse(responseCode = "400", description = "Invalid page number or not enough ingredients in the fridge"),
            @ApiResponse(responseCode = "404", description = "Foodie not found")
    })
    public ResponseEntity<SliceRecipeDTO<RecipeSuggestionDTO>> getRecommendations(@PathVariable int pageNumber) {
        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        SliceRecipeDTO<RecipeSuggestionDTO> recipes = smartFridgeService.getRecommendations(authFoodie.getUsername(), pageNumber);

        if (recipes.getContent().isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(recipes);
    }


    /**
     * Retrieves detailed information about a specific recipe suggested by the smart fridge.
     * @param id the unique identifier of the requested recipe
     * @return a {@link ResponseEntity} containing a {@link ShowRecipeDTO} with the recipe details
     * @see SmartFridgeService#getFridgeRecipeById(String)
     */
    @GetMapping("/recipe/{id}")
    @Operation(summary = "Get recipe details", description = "Retrieves the full details of a specific recipe recommended by the smart fridge.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recipe details successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Recipe not found")
    })
    public ResponseEntity<ShowRecipeDTO> getRecipe(@PathVariable String id){
        ShowRecipeDTO recipe = smartFridgeService.getFridgeRecipeById(id);
        return ResponseEntity.ok(recipe);
    }
}

