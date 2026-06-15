package it.unipi.MySmartRecipeBook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.MySmartRecipeBook.dto.recipe.*;
import it.unipi.MySmartRecipeBook.service.RecipeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for viewing and searching recipes with public access.
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
     * Retrieves the detailed information of a specified recipe.
     * @param id the unique identifier of the target recipe
     * @return a {@link ResponseEntity} containing a {@link ShowRecipeDTO} with the recipe's details
     * @see RecipeService#getRecipeById(String)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get recipe's details", description = "Retrieves the full details of a specific recipe using its unique id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recipe details successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Recipe not found")
    })
    public ResponseEntity<ShowRecipeDTO> getRecipe (@PathVariable String id) {

        ShowRecipeDTO standardRecipeDTO = recipeService.getRecipeById(id);
        return ResponseEntity.ok(standardRecipeDTO);
    }


    /**
     * Searches for recipes by title (using substring matching) to display on the home page.
     * Returns a paginated list with five recipes per page.
     * @param title the substring to search for within recipe titles
     * @param pageNumber the requested page number for pagination
     * @return a {@link ResponseEntity} containing a list of {@link UserPreviewRecipeDTO} with the paginated search results
     * @see RecipeService#getRecipeByTitle(String, int)
     */
    @GetMapping("/search/{title}/{pageNumber}")
    @Operation(summary = "Search recipes by title", description = "Searches for recipes containing the specified string in their title. Returns 5 results per page.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recipes successfully retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid page number or search title")
    })
    public ResponseEntity<SliceRecipeDTO<UserPreviewRecipeDTO>> getRecipeByTitle(@PathVariable String title, @PathVariable int pageNumber){

        SliceRecipeDTO<UserPreviewRecipeDTO> recipes_list = recipeService.getRecipeByTitle(title, pageNumber);
        return ResponseEntity.ok(recipes_list);
    }


    /**
     * Retrieves a paginated list of the newest recipes uploaded to the system.
     * @param pageNumber the requested page number
     * @return a {@link ResponseEntity} containing a list of {@link UserPreviewRecipeDTO} with the paginated preview of the newest recipes
     * @see RecipeService#getNewestRecipe(int)
     */
    @GetMapping("/homeRecipe/{pageNumber}")
    @Operation(summary = "View newest recipes", description = "Retrieves a paginated list of the most recently uploaded recipes in the system.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Newest recipes successfully retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid page number")
    })
    public ResponseEntity<SliceRecipeDTO<UserPreviewRecipeDTO>> getHomeRecipe (@PathVariable int pageNumber){

        SliceRecipeDTO<UserPreviewRecipeDTO> recipe_list = recipeService.getNewestRecipe(pageNumber);
        return ResponseEntity.ok(recipe_list);
    }


    /**
     * Retrieves a paginated list of recipes filtered by a specific category.
     * @param pageNumber the requested page number
     * @param category the category to filter the recipes by
     * @return a {@link ResponseEntity} containing a list of {@link UserPreviewRecipeDTO} with the paginated recipes of the selected category
     * @see RecipeService#getByCategory(int, String)
     */
    @GetMapping("/category/{category}/{pageNumber}")
    @Operation(summary = "Search by category", description = "Filters and returns a paginated list of recipes belonging to a specific category.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recipes successfully retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid page number or category filter")
    })
    public ResponseEntity<SliceRecipeDTO<UserPreviewRecipeDTO>> getRecipeByCategory (@PathVariable int pageNumber, @PathVariable String category){

        SliceRecipeDTO<UserPreviewRecipeDTO> recipe_list = recipeService.getByCategory(pageNumber, category);
        return ResponseEntity.ok(recipe_list);
    }


    /**
     * Retrieves a paginated list of recipes created by a specific chef.
     * @param pageNumber the requested page number
     * @param chefId the unique identifier of the target chef
     * @return a {@link ResponseEntity} containing a list of {@link ChefPreviewRecipeDTO} with the paginated chef's recipes
     * @throws IllegalArgumentException if the page number is less than or equal to 0
     * @see RecipeService#getChefRecipePage(int, String)
     */
    @GetMapping("/chef/{chefId}/{pageNumber}")
    @Operation(summary = "View chef's recipes", description = "Retrieves a paginated list of all recipes published by a specific Chef, identified by their id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chef's recipes successfully retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid page number provided"),
            @ApiResponse(responseCode = "404", description = "Chef not found")
    })
    public ResponseEntity<SliceRecipeDTO<ChefPreviewRecipeDTO>> getChefRecipes (@PathVariable int pageNumber, @PathVariable String chefId){

        if(pageNumber <= 0){
            throw new IllegalArgumentException("Invalid parameters");
        }

        SliceRecipeDTO<ChefPreviewRecipeDTO>recipe_list = recipeService.getChefRecipePage(pageNumber, chefId);
        return ResponseEntity.ok(recipe_list);
    }


    /**
     * Retrieves the top-rated recipe for each available category in the application,
     * determined by the highest number of saves.
     * @return a {@link ResponseEntity} containing a {@link List} of {@link TopRecipeByCategoryDTO} representing the top recipe per category
     * @see RecipeService#getCategoryTrend()
     */
    @GetMapping("/categoryTrend")
    @Operation(summary = "Retrieve top recipe per category", description = "Retrieves the single top recipe for each application category, selected based on the highest number of saves.")
    @ApiResponse(responseCode = "200", description = "Top recipes per category successfully retrieved")
    public ResponseEntity<List<TopRecipeByCategoryDTO>> getCategoryTrends (){

        List<TopRecipeByCategoryDTO> category = recipeService.getCategoryTrend();
        return ResponseEntity.ok(category);
    }


}
