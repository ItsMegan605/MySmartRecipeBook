package it.unipi.MySmartRecipeBook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.MySmartRecipeBook.dto.recipe.ShowRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.SliceRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.users.ChefPreviewDTO;
import it.unipi.MySmartRecipeBook.service.RecipeService;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * Recipe's Controller
 */
@RestController
@RequestMapping("/api/recipes")
@Tag(name = "Recipes", description = "Endpoints for viewing and searching recipes (Public Access)")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    /**
     * Shows all the details of a specified recipe.
     * @param id user id
     * @see RecipeService#getRecipeById(String)
     * @return ResponseEntity ok message 
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a recipe's details", description = "gives the recipe's details from its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recipe found"),
            @ApiResponse(responseCode = "404", description = "Recipe not found")
    })
    public ResponseEntity<ShowRecipeDTO> getRecipe (@PathVariable String id) {

        ShowRecipeDTO standardRecipeDTO = recipeService.getRecipeById(id);
        return ResponseEntity.ok(standardRecipeDTO);
    }

    /**
     * Function to search a recipe by title in the home page (the research will be done searching sub-strings).
     * Five recipes at a time will be shown
     * @param title
     * @param pageNumber
     * @see RecipeService#getRecipeByTitle(String, int) 
     * @return ResponseEntity ok message
     */
    @GetMapping("/search")
    @Operation(summary = "Search recipes by title", description = "Searches for recipes containing the specified string in the title. Returns 5 results per page.")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SliceRecipeDTO> getRecipeByTitle(@RequestParam String title, @RequestParam(defaultValue = "1") int pageNumber){

        SliceRecipeDTO recipes_list = recipeService.getRecipeByTitle(title, pageNumber);
        return ResponseEntity.ok(recipes_list);
    }

    /**
     * Home page for the recipes with the newest recipes uploaded
     * @param pageNumber
     * @see RecipeService#getNewestRecipe(int) 
     * @return ResponseEntity ok message 
     */
    @GetMapping("/homeRecipe")
    @Operation(summary = "View newest recipes", description = "Returns the list of the most recently uploaded recipes in the system, divided into pages.")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SliceRecipeDTO> getHomeRecipe (@RequestParam(defaultValue = "1") int pageNumber){

        SliceRecipeDTO recipe_list = recipeService.getNewestRecipe(pageNumber);
        return ResponseEntity.ok(recipe_list);
    }

    /**
     * Function to order the user's saved recipes by a specific category
     * @param pageNumber
     * @param category
     * @see RecipeService#getByCategory(int, String) 
     * @return ResponseEntity ok message 
     */
    @GetMapping("/category")
    @Operation(summary = "Search by category", description = "Filters and returns recipes belonging to a specific category.")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SliceRecipeDTO> getRecipeByCategory (@RequestParam(defaultValue = "1") int pageNumber, @RequestParam String category){

        SliceRecipeDTO recipe_list = recipeService.getByCategory(pageNumber, category);
        return ResponseEntity.ok(recipe_list);
    }

    /**
     * Method to get recipes created a chef
     * @param pageNumber
     * @param chefId
     * @see RecipeService#getChefRecipePage(int, String) 
     * @return ResponseEntity ok message 
     */
    @GetMapping("/chef")
    @Operation(summary = "View a Chef's recipes", description = "Returns all recipes published by a specific Chef, identified by their ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400")
    })
    public ResponseEntity<SliceRecipeDTO> getChefRecipes (@RequestParam(defaultValue = "1") int pageNumber, @RequestParam String chefId){

        if(pageNumber <= 0){
            throw new IllegalArgumentException("Invalid parameters");
        }

        SliceRecipeDTO recipe_list = recipeService.getChefRecipePage(pageNumber, chefId);
        return ResponseEntity.ok(recipe_list);
    }


}
