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
    @Operation(summary = "Retrieve foodie's information", description = "Fetches the personal information of the currently authenticated foodie.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Information successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Foodie not found")
    })
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
    @Operation(summary = "Change foodie's personal information", description = "Updates personal details like name, surname, email, password, and birthdate. Username cannot be modified.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Information successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input parameters or age constraint violated"),
            @ApiResponse(responseCode = "404", description = "Foodie not found")
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
    @Operation(summary = "Delete foodie's profile", description = "Permanently removes the currently authenticated foodie's profile and updates related statistics.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Foodie not found")
    })
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
    @Operation(summary = "Add a recipe to favourites", description = "Saves a specific recipe preview to the foodie's favorites list.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recipe successfully added to favourites"),
            @ApiResponse(responseCode = "404", description = "Foodie or Recipe not found"),
            @ApiResponse(responseCode = "409", description = "Recipe has already been saved by the foodie")
    })
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
    @Operation(summary = "Remove a recipe from favourites", description = "Removes a specific recipe from the foodie's favorites list.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recipe successfully removed from favourites"),
            @ApiResponse(responseCode = "404", description = "Foodie or Recipe not found, or recipe not in favorites")
    })
    public ResponseEntity<String> removeSavedRecipe(@PathVariable String recipeId) {

        foodieService.removeSavedRecipe(recipeId);
        return ResponseEntity.ok("Recipe has been successfully removed from favourites");
    }


    /**
     * Retrieves the foodie's favorite recipes filtered by a specified category, difficulty, or saving date.
     * @param filter the filtering criterion
     * @param pageNumber the number of the page to retrieve
     * @return a {@link ResponseEntity} containing a list of {@link FoodiePreviewRecipeDTO} with the paginated preview of the recipes
     * @see FoodieService#getRecipeByCategory(String, int)
     */
    @GetMapping("/getRecipe/{filter}/{pageNumber}")
    @Operation(summary = "Retrieve favourite recipes filtered", description = "Fetches a paginated list of the foodie's favorite recipes, filtered by category, difficulty, or saving date.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recipes successfully retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid filter or page number"),
            @ApiResponse(responseCode = "404", description = "Foodie not found")
    })
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
    @Operation(summary = "Retrieve saved recipe details", description = "Fetches the full details of a specific recipe saved in the foodie's favorites.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recipe details successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Foodie or Recipe not found, or recipe not in favorites")
    })
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
    @Operation(summary = "Search for chefs by surname", description = "Retrieves a list of chefs whose surname matches the provided search string.")
    @ApiResponse(responseCode = "200", description = "List of matching chefs successfully retrieved")
    public ResponseEntity<List<ChefPreviewDTO>> getChef (@RequestParam String chefSurname){
        List<ChefPreviewDTO> chefList = foodieService.getChefList(chefSurname);
        return ResponseEntity.ok(chefList);
    }


    /**
     * Retrieves a list of recipes that are most similar to the specified one.
     * Similarity is calculated based on shared ingredients.
     * @param recipeId the unique identifier of the target recipe
     * @return a {@link ResponseEntity} containing a list of up to three {@link RecipeSuggestionDTO} with the preview of the similar recipes
     * @see FoodieService#getSimilarRecipes(String) 
     */
    @GetMapping("/similarRecipes/{recipeId}")
    @Operation(summary = "Show similar recipes", description = "Show the three most similar recipes to the one currently visualized")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Similar recipes successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Recipe not found or not approved yet")
    })
    public ResponseEntity<List<RecipeSuggestionDTO>> similarRecipes(@PathVariable String recipeId) {
        List<RecipeSuggestionDTO> similarRecipes = foodieService.getSimilarRecipes(recipeId);
        return ResponseEntity.ok(similarRecipes);
    }


    /**
     * Retrieves a list of similar chefs based on the ingredients they use.
     * @param chefId the unique identifier of the chef to compare against
     * @return a {@link ResponseEntity} containing a list of {@link ChefInfoDTO} representing the similar chefs
     * @see FoodieService#getSimilarChefs(String)
     */
    @GetMapping("/findSimilarChef/{chefId}")
    @Operation(summary = "Show similar chefs", description = "Show similar chefs based on ingredients used")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Similar chefs successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Chef not found or not approved yet")
    })
    public ResponseEntity<List<ChefInfoDTO>> similarChefs(@PathVariable String chefId) {
        List<ChefInfoDTO> similarChefs = foodieService.getSimilarChefs(chefId);
        return ResponseEntity.ok(similarChefs);
    }
}
