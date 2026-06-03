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
     * Post Method to approve a pending recipe
     * @param recipeId - id of the recipe
     * @see AdminService#saveRecipe(String)
     * @return ResponseEntity with approval message
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
     * Delete Method to discard a pending recipe
     * @param recipeId - id of the recipe
     * @see AdminService#discardRecipe(String)
     * @return ResponseEntity with  message
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
     * Post method to approve a chef
     * @param chefUsername - chef username
     * @see AdminService#approveChef(String)
     * @return ResponseEntity with  message
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
     * Post method to discard a chef's request to register
     * @param chefUsername - chef username
     * @see AdminService#declineChef(String)
     * @return ResponseEntity with  message
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
     * Get method for a page with a list of recipes that are pending admin approval.
     * @param page the page number to retrieve
     * @see AdminService#showPendingRecipes(int)
     * @return a ResponseEntity containing a slice of pending recipes
     */
    @GetMapping("/showRecipes/{page}")
    @Operation(summary = "Get pending recipes", description = "Retrieves a paginated list of all recipes currently awaiting admin approval.")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SliceRecipeDTO<PendingRecipeDTO>> showRecipe (@PathVariable int page){
        SliceRecipeDTO<PendingRecipeDTO> recipeList = adminService.showPendingRecipes(page);
        return ResponseEntity.ok(recipeList);
    }

    /**
     * Get method for a paginated list of chef registration requests that are pending admin approval.
     * @param page the page number to retrieve
     * @see AdminService#showPendingChefs(int)
     * @return a ResponseEntity containing a slice of pending chef profiles
     */
    @GetMapping("/showChefs/{page}")
    @Operation(summary = "Get pending chef requests", description = "Retrieves a paginated list of users who have requested to become chefs and are awaiting approval.")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SliceRecipeDTO<PendingChefDTO>> showChefs (@PathVariable int page){
        SliceRecipeDTO<PendingChefDTO> chefList = adminService.showPendingChefs(page);
        return ResponseEntity.ok(chefList);
    }

    /**
     * Get method to get the number of users that registered in past months
     * @see AdminService#getMonthlyFoodies()
     * @return ResponseEntity with ok message
     */
    @GetMapping ("/monthlyFoodies")
    @Operation(summary = "Get monthly user registration analytics", description = "Provides statistical data on the number of new foodies registered over the past months.")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<List<YearAnalyticsDTO>> getMonthlyFoodies() {
        List<YearAnalyticsDTO> stats = adminService.getMonthlyFoodies();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get method to see the trending of the different categories
     * @see AdminService#getCategoryTrends()
     * @return ResponseEntity with ok message
     *
     */
    @GetMapping("/categoryTrends")
    @Operation(summary = "Get category trend analytics", description = "Provides insights into the popularity and trends of different recipe categories.")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<List<TrendAnalyticsDTO>> getCategoryTrends() {
        return ResponseEntity.ok(adminService.getCategoryTrends());
    }

    /**
     * Bayesian Chef Ranking visible to Foodies
     * @see AdminService#getBayesianRanking()
     * @return ResponseEntity ok message
     */
    @GetMapping("/chefsRanking")
    @Operation(summary = "Get Bayesian Chef Ranking visible to foodies")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<java.util.List<ChefRankAnalyticsDTO>> getChefRanking() {

        return ResponseEntity.ok(
                adminService.getBayesianRanking()
        );
    }

    /**
     * Get method to retrieve detailed information about a specific chef.
     * @param username the username of the chef
     * @see AdminService#seeChefDetails(String)
     * @return ResponseEntity containing the chef's details
     */
    @GetMapping("/details/chef/{username}")
    @Operation(summary = "Get chef details")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<RegisteredUserInfoDTO> seeChefDetails (@PathVariable String username){
        return ResponseEntity.ok(adminService.seeChefDetails(username));
    }

    /**
     * Get method to retrieve detailed information about a specific recipe.
     * @param recipeId - recipe ID
     * @see AdminService#seeRecipeDetails(String)
     * @return ResponseEntity containing the recipe's details
     */
    @GetMapping("/details/recipe/{recipeId}")
    @Operation(summary = "Get recipe details")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<ShowRecipeDTO> seeRecipeDetails (@PathVariable String recipeId){
        return ResponseEntity.ok(adminService.seeRecipeDetails(recipeId));
    }
}

