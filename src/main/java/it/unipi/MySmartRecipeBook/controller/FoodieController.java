package it.unipi.MySmartRecipeBook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.MySmartRecipeBook.dto.recipe.ShowRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.SliceRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.users.RegisteredUserInfoDTO;
import it.unipi.MySmartRecipeBook.dto.users.UpdateFoodieDTO;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import it.unipi.MySmartRecipeBook.service.FoodieService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import it.unipi.MySmartRecipeBook.dto.ChefRankAnalyticsDTO;
import it.unipi.MySmartRecipeBook.service.ChefService;


import java.time.LocalDate;
import java.time.Period;

/**
 * Foodie's controller
 */
@RestController
@RequestMapping("/api/foodies")
@PreAuthorize("hasRole('FOODIE')")
@Tag(name = "Foodie", description = "Endpoints for managing Foodie profiles and their favorite recipes")
public class FoodieController {

    private final FoodieService foodieService;
    private final ChefService chefService;

    public FoodieController(FoodieService foodieService, ChefService chefService) {
        this.foodieService = foodieService;
        this.chefService = chefService;
    }

    /**
     * Retrieve foodie's information
     * @see FoodieService#getById()
     * @return ResponseEntity ok message
     */
    @GetMapping("/info")
    @Operation(summary = "Retrieve foodie's information")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<RegisteredUserInfoDTO> getInfo(){

        return ResponseEntity.ok(foodieService.getById());
    }


    /**
     * Change foodie's information
     * @param updates the parameter that we want to change
     * @see FoodieService#updateFoodie(UpdateFoodieDTO)
     * @return ResponseEntity ok message
     */
    @PostMapping("/changeInfo")
    @Operation(summary = "Change foodie's information")
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
     * Delete foodie's Profile
     * @see FoodieService#deleteFoodie()
     * @return ResponseEntity with message
     */
    @DeleteMapping("/deleteProfile")
    @Operation(summary = "Delete foodie's profile")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<String> deleteProfile() {

        foodieService.deleteFoodie();
        return ResponseEntity.ok("Foodie has been successfully deleted");
    }


    /**
     * Add a recipe to foodie's favourites
     * @param recipeId
     * @see FoodieService#saveRecipe(String, String)
     * @return ResponseEntity with message
     */
    @PostMapping("/addFavourite/{recipeId}")
    @Operation(summary = "Add a recipe to foodie's favourites")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<String> saveRecipe (@PathVariable String recipeId) {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        foodieService.saveRecipe(authFoodie.getId(), recipeId);
        return ResponseEntity.ok("Recipe has been successfully added to favourites");
    }


    /**
     * Remove a recipe from foodie's favourites
     * @param recipeId
     * @see FoodieService#removeSavedRecipe(String)
     * @return ResponseEntity ok message
     */
    @DeleteMapping("/removeFavourite/{recipeId}")
    @Operation(summary = "Remove a recipe from foodie's favourites")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<String> removeSavedRecipe(@PathVariable String recipeId) {

        foodieService.removeSavedRecipe(recipeId);
        return ResponseEntity.ok("Recipe has been successfully removed from favourites");
    }


    /**
     * Order favourites recipes by filter
     * @param category
     * @param numPage
     * @see FoodieService#getRecipeByCategory(String, int)
     * @return ResponseEntity ok message
     */
    @GetMapping("/getRecipe/{category}/{numPage}")
    @Operation(summary = "Order favourite recipes by category filter")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SliceRecipeDTO> getRecipeByCategory (@PathVariable String category,
                                                                            @PathVariable int numPage) {
        SliceRecipeDTO recipeList = foodieService.getRecipeByCategory(category, numPage);
        return ResponseEntity.ok(recipeList);
    }

    /**
     * Get method to get the details of a specific recipe for a Foodie.
     * @param id
     * @see FoodieService#getRecipeFoodieById(String)
     * @return a ResponseEntity containing the ShowRecipeDTO with the recipe details
     */
    @GetMapping("/recipe/{id}")
    @Operation(summary = "Get the details of a specific recipe for a foodie")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<ShowRecipeDTO> getRecipeById (@PathVariable String id){
        ShowRecipeDTO recipe = foodieService.getRecipeFoodieById(id);
        return ResponseEntity.ok(recipe);
    }


    /**
     * Bayesian Chef Ranking visible to Foodies
     * @see ChefService#getChefRankingForFoodie()
     * @return ResponseEntity ok message
     */
    @GetMapping("/chefsRanking")
    @Operation(summary = "Get Bayesian Chef Ranking visible to foodies")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<java.util.List<ChefRankAnalyticsDTO>> getChefRanking() {

        return ResponseEntity.ok(
                chefService.getChefRankingForFoodie()
        );
    }
}
