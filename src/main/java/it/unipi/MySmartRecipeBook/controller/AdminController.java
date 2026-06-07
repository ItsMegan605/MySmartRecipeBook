package it.unipi.MySmartRecipeBook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.MySmartRecipeBook.dto.*;
import it.unipi.MySmartRecipeBook.dto.recipe.PendingRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.ShowRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.SliceRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.users.PendingChefDTO;
import it.unipi.MySmartRecipeBook.dto.users.RegisteredUserInfoDTO;
import it.unipi.MySmartRecipeBook.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Endpoints for platform administration (Requires ADMIN role)")
public class AdminController {

    private final AdminService adminService;
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Approves a chef's pending recipe
     * @param recipeId the unique identifier of the recipe to approve
     * @return a {@link ResponseEntity} containing the approval success message
     * @see AdminService#saveRecipe(String)
     */
    @PostMapping("/approve/{id}")
    @Operation(summary = "Approve a pending recipe", description = "Approves a recipe that is currently pending waiting for admin approval")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recipe successfully approved"),
            @ApiResponse(responseCode = "404", description = "Recipe not found")
    })
    public ResponseEntity<String> approveRecipe(@PathVariable("id") String recipeId) {
        adminService.saveRecipe(recipeId);
        return ResponseEntity.ok("Recipe approved");
    }


    /**
     * Discards a chef's pending recipe.
     * @param recipeId the unique identifier of the recipe to discard
     * @return a {@link ResponseEntity} containing the discard success message
     * @see AdminService#discardRecipe(String)
     */
    @DeleteMapping("/discard/{id}")
    @Operation(summary = "Discard a pending recipe", description = "Rejects and permanently deletes a recipe that is pending approval.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404")
    })
    public ResponseEntity<String> discardRecipe(@PathVariable("id") String recipeId) {
        adminService.discardRecipe(recipeId);
        return ResponseEntity.ok("Recipe successfully discarded");
    }


    /**
     * Approves a pending chef's registration request
     * @param chefUsername unique username of the chef to approve
     * @return {@link ResponseEntity} containing the approval success message
     * @see AdminService#approveChef(String)
     */
    @PostMapping("/approveChef/{username}")
    @Operation(summary = "Approve a chef registration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chef successfully approved"),
            @ApiResponse(responseCode = "404", description = "Chef not found or not pending")
    })
    public ResponseEntity<String> approveChef(@PathVariable("username") String chefUsername) {
        adminService.approveChef(chefUsername);
        return ResponseEntity.ok("Chef successfully added by admin");
    }


    /**
     * Discards a pending chef's registration request
     * @param chefUsername unique username of the chef to discard
     * @return {@link ResponseEntity} containing the discard success message
     * @see AdminService#declineChef(String)
     */
    @DeleteMapping("/discardChef/{username}")
    @Operation(summary = "Decline a chef registration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404")
    })
    public ResponseEntity<String> discardChef(@PathVariable("username") String chefUsername) {
        adminService.declineChef(chefUsername);
        return ResponseEntity.ok("Chef declined by admin");
    }


    /**
     * Retrieves a paginated list of pending recipes awaiting admin approval.
     * @param pageNumber the page number to retrieve
     * @return a {@link ResponseEntity} containing a {@link SliceRecipeDTO} with the pending recipes
     * @see AdminService#showPendingRecipes(int)
     */
    @GetMapping("/showRecipes/{pageNumber}")
    @Operation(summary = "Get pending recipes", description = "Retrieves a paginated list of all recipes currently awaiting admin approval.")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SliceRecipeDTO<PendingRecipeDTO>> showRecipe (@PathVariable int pageNumber){
        SliceRecipeDTO<PendingRecipeDTO> recipeList = adminService.showPendingRecipes(pageNumber);
        return ResponseEntity.ok(recipeList);
    }


    /**
     * Retrieves a paginated list of pending chefs awaiting admin approval.
     * @param pageNumber the page number to retrieve
     * @return a {@link ResponseEntity} containing a {@link SliceRecipeDTO} with the pending chefs
     * @see AdminService#showPendingChefs(int)
     */
    @GetMapping("/showChefs/{pageNumber}")
    @Operation(summary = "Get pending chef requests", description = "Retrieves a paginated list of users who have requested to become chefs and are awaiting approval.")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SliceRecipeDTO<PendingChefDTO>> showChefs (@PathVariable int pageNumber){
        SliceRecipeDTO<PendingChefDTO> chefList = adminService.showPendingChefs(pageNumber);
        return ResponseEntity.ok(chefList);
    }


    /**
     * Retrieves the detailed information about a specific pending chef.
     * @param username the unique username of the chef to retrieve
     * @return {@link ResponseEntity} containing a {@link RegisteredUserInfoDTO} with the chef's details
     * @see AdminService#seeChefDetails(String)
     */
    @GetMapping("/details/chef/{username}")
    @Operation(summary = "Get chef details")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<RegisteredUserInfoDTO> seeChefDetails (@PathVariable String username){
        return ResponseEntity.ok(adminService.seeChefDetails(username));
    }


    /**
     * Retrieves the detailed information about a specific pending recipe.
     * @param recipeId the unique identifier of the recipe to retrieve
     * @return {@link ResponseEntity} containing a {@link ShowRecipeDTO} with the recipe's details
     * @see AdminService#seeRecipeDetails(String)
     */
    @GetMapping("/details/recipe/{recipeId}")
    @Operation(summary = "Get recipe details")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<ShowRecipeDTO> seeRecipeDetails (@PathVariable String recipeId){
        return ResponseEntity.ok(adminService.seeRecipeDetails(recipeId));
    }


    /**
     * Retrieves the statistical data regarding the number of new foodies registered over the past months.
     * @return a {@link ResponseEntity} containing a list of {@link YearAnalyticsDTO} representing the monthly registration statistics
     * @see AdminService#getMonthlyFoodies()
     */
    @GetMapping ("/monthlyFoodies")
    @Operation(summary = "Get monthly user registration analytics", description = "Provides statistical data on the number of new foodies registered over the past months.")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<List<YearAnalyticsDTO>> getMonthlyFoodies() {
        List<YearAnalyticsDTO> stats = adminService.getMonthlyFoodies();
        return ResponseEntity.ok(stats);
    }


    /**
     * Retrieves the analytics regarding the popularity and trends of different recipe categories.
     * @return a {@link ResponseEntity} containing a list of {@link TrendAnalyticsDTO} representing the category trends
     * @see AdminService#getCategoryTrends()
     */
    @GetMapping("/categoryTrends")
    @Operation(summary = "Get category trend analytics", description = "Provides insights into the popularity and trends of different recipe categories.")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<List<TrendAnalyticsDTO>> getCategoryTrends() {
        return ResponseEntity.ok(adminService.getCategoryTrends());
    }

    /**
     * Retrieves the ranking of chefs calculated using a Bayesian algorithm.
     * @return a {@link ResponseEntity} containing a list of {@link ChefRankAnalyticsDTO} representing the Bayesian chef ranking
     * @see AdminService#getBayesianRanking()
     */
    @GetMapping("/chefsRanking")
    @Operation(summary = "Get Bayesian Chef Ranking visible to foodies")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<java.util.List<ChefRankAnalyticsDTO>> getChefRanking() {
        return ResponseEntity.ok(adminService.getBayesianRanking());
    }

}

