package it.unipi.MySmartRecipeBook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.MySmartRecipeBook.dto.recipe.FoodiePreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.RecipeSuggestionDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.ShowRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.SliceRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.users.*;
import it.unipi.MySmartRecipeBook.service.FoodieService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.time.Period;
import java.util.List;

/**
 * Foodie's controller
 */
@RestController
@RequestMapping("/api/foodies")
@PreAuthorize("hasRole('FOODIE')")
@Tag(name = "Foodie", description = "Endpoints for managing Foodie profiles and their favorite recipes")
public class FoodieController {

    private final FoodieService foodieService;
    public FoodieController(FoodieService foodieService) {

        this.foodieService = foodieService;
    }


    /**
     * Retrieves the personal information of the currently authenticated foodie.
     * @return a {@link ResponseEntity} containing a {@link RegisteredUserInfoDTO} with the foodie's personal information
     * @see FoodieService#getById()
     */
    @GetMapping("/info")
    @Operation(summary = "Retrieve foodie's information")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<RegisteredUserInfoDTO> getInfo(){

        return ResponseEntity.ok(foodieService.getById());
    }


    /**
     * Changes the personal information of the currently authenticated foodie.
     * @param updates a {@link UpdateFoodieDTO} containing the foodie's personal information to update
     * @return a {@link ResponseEntity} containing a {@link RegisteredUserInfoDTO} with the updated foodie's personal information
     * @throws IllegalArgumentException if the updated birthdate results in an age under 15
     * @see FoodieService#updateFoodie(UpdateFoodieDTO)
     */
    @PostMapping("/changeInfo")
    @Operation(summary = "Change foodie's personal information")
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400")
    })
    public ResponseEntity<RegisteredUserInfoDTO> changeInfo (@Valid @RequestBody UpdateFoodieDTO updates){

        if(updates.getBirthdate() != null && Period.between(updates.getBirthdate(), LocalDate.now()).getYears() < 15){
            throw new IllegalArgumentException("You must be at least 15 years old to use this service");
        }

        return ResponseEntity.ok(foodieService.updateFoodie(updates));
    }


    /**
     * Deletes the profile of the authenticated foodie.
     * @return a {@link ResponseEntity} with a success message confirming the deletion
     * @see FoodieService#deleteFoodie()
     */
    @DeleteMapping("/deleteProfile")
    @Operation(summary = "Delete foodie's profile")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<String> deleteProfile() {

        foodieService.deleteFoodie();
        return ResponseEntity.ok("Foodie has been successfully deleted");
    }


    /**
     * Adds a recipe to foodie's favourites.
     * @param recipeId the unique identifier of the target recipe
     * @return a {@link ResponseEntity} with a success message confirming the insertion
     * @see FoodieService#saveRecipe(String)
     */
    @PostMapping("/addFavourite/{recipeId}")
    @Operation(summary = "Add a recipe to foodie's favourites")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<String> saveRecipe (@PathVariable String recipeId) {

        foodieService.saveRecipe(recipeId);
        return ResponseEntity.ok("Recipe has been successfully added to favourites");
    }


    /**
     * Removes a recipe from the foodie's favourites.
     * @param recipeId the unique identifier of the target recipe
     * @return a {@link ResponseEntity} with a success message confirming the removal
     * @see FoodieService#removeSavedRecipe(String)
     */
    @DeleteMapping("/removeFavourite/{recipeId}")
    @Operation(summary = "Remove a recipe from foodie's favourites")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<String> removeSavedRecipe(@PathVariable String recipeId) {

        foodieService.removeSavedRecipe(recipeId);
        return ResponseEntity.ok("Recipe has been successfully removed from favourites");
    }

    // TODO: cambiare endpoint
    /**
     * Retrieves the foodie's favorite recipes filtered by a specified category, difficulty, or saving date.
     * @param filter the filtering criterion
     * @param pageNumber the number of the page to retrieve
     * @return a {@link ResponseEntity} containing a list of {@link FoodiePreviewRecipeDTO} with the paginated preview of the recipes
     * @see FoodieService#getRecipeByCategory(String, int)
     */
    @GetMapping("/getRecipe/{filter}/{pageNumber}")
    @Operation(summary = "Retrieve the foodie's favourite recipes filtered by category")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SliceRecipeDTO<FoodiePreviewRecipeDTO>> getRecipeByCategory (@PathVariable String filter,
                                                                                       @PathVariable int pageNumber) {
        SliceRecipeDTO<FoodiePreviewRecipeDTO> recipeList = foodieService.getRecipeByCategory(filter, pageNumber);
        return ResponseEntity.ok(recipeList);
    }


    /**
     * Retrieves the detailed information of the specified recipe from the foodie's favorites.
     * @param id the unique identifier of the target recipe
     * @return a {@link ResponseEntity} containing a {@link ShowRecipeDTO} with the recipe details
     * @see FoodieService#getRecipeFoodieById(String)
     */
    @GetMapping("/recipe/{id}")
    @Operation(summary = "Retrieve the detailed information of the specified recipe from the foodie's favorites")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<ShowRecipeDTO> getRecipeById (@PathVariable String id){
        ShowRecipeDTO recipe = foodieService.getRecipeFoodieById(id);
        return ResponseEntity.ok(recipe);
    }


    /**
     * Retrieves the list of matching chefs by their surname.
     * @param chefSurname the target surname to search for
     * @return a {@link ResponseEntity} containing a list of {@link ChefPreviewDTO} with the previews of the matching chefs
     * @see FoodieService#getChefList(String)
     */
    @GetMapping("/matchingChef")
    @Operation(summary = "Search for chefs by surname")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<List<ChefPreviewDTO>> getChef (@RequestParam String chefSurname){
        List<ChefPreviewDTO> chefList = foodieService.getChefList(chefSurname);
        return ResponseEntity.ok(chefList);
    }


    /**
     * Retrieves the ranking of the top 3 chefs for each available recipe category.
     * @return a {@link ResponseEntity} containing the list of {@link TopChefDTO} with the preview of the matching chefs
     * @see FoodieService#getTopChef()
     */
    @GetMapping("/TopChef")
    @Operation(summary = "Show top 3 chefs per category")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<List<TopChefDTO>> getTopChef() {
        List<TopChefDTO> topChefs = foodieService.getTopChef();
        return ResponseEntity.ok(topChefs);
    }

    /**
     * Retrieves a list of recipes that are most similar to the specified recipe.
     * Similarity is calculated based on shared ingredients.
     * @param recipeId the unique identifier of the target recipe
     * @return a {@link ResponseEntity} containing a list of up to three {@link RecipeSuggestionDTO} with the preview of the similar recipes
     * @see FoodieService#getSimilarRecipes(String) 
     */
    @GetMapping("/similarRecipes/{recipeId}")
    @Operation(summary = "Show the three most similar recipes to the one currently visualized")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<List<RecipeSuggestionDTO>> similarRecipes(@PathVariable String recipeId) {
        List<RecipeSuggestionDTO> similarRecipes = foodieService.getSimilarRecipes(recipeId);
        return ResponseEntity.ok(similarRecipes);
    }
}
