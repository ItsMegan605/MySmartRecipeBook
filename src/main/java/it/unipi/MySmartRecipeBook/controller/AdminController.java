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
 * Admin's REST Controller.
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
            @ApiResponse(responseCode = "404", description = "Pending recipe not found")
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
            @ApiResponse(responseCode = "200", description = "Recipe successfully discarded"),
            @ApiResponse(responseCode = "404", description = "Pending recipe not found")
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
    @Operation(summary = "Approve a chef registration", description = "Approves a pending chef registration request.")
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
    @Operation(summary = "Decline a chef registration", description = "Declines a pending chef registration request.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chef registration successfully declined"),
            @ApiResponse(responseCode = "404", description = "Chef not found or not pending")
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pending recipes successfully retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid page number"),
            @ApiResponse(responseCode = "404", description = "Admin not found")
    })
    public ResponseEntity<SliceRecipeDTO<PendingRecipeDTO>> showRecipes (@PathVariable int pageNumber){
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pending chefs successfully retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid page number"),
            @ApiResponse(responseCode = "404", description = "Admin not found")
    })
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
    @Operation(summary = "Get chef details", description = "Retrieves the details of a specific pending chef.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chef details successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Pending chef not found")
    })
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
    @Operation(summary = "Get recipe details", description = "Retrieves the details of a specific pending recipe.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recipe details successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Pending recipe not found")
    })
    public ResponseEntity<ShowRecipeDTO> seeRecipeDetails (@PathVariable String recipeId){
        return ResponseEntity.ok(adminService.seeRecipeDetails(recipeId));
    }


    // TODO: controllare endpoint
    /**
     * Retrieves the statistics regarding the number of new foodies registered over the past months.
     * @param year the specific year to retrieve analytics for
     * @return a {@link ResponseEntity} containing a {@link YearAnalyticsDTO} representing the monthly registration statistics
     */
    @GetMapping ("/monthlyFoodies/{year}")
    @Operation(summary = "Get monthly user registration analytics", description = "Provides statistical data on the number of new foodies registered over the past months for a specific year.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Monthly analytics successfully retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid year provided")
    })
    public ResponseEntity<YearAnalyticsDTO> getMonthlyFoodies(@PathVariable int year) {
        YearAnalyticsDTO stats = adminService.getMonthlyFoodies(year);
        return ResponseEntity.ok(stats);
    }


    /**
     * Retrieves the analytics regarding the popularity and trends of different recipe categories.
     * @return a {@link ResponseEntity} containing a list of {@link TrendAnalyticsDTO} representing the category trends
     * @see AdminService#getCategoryTrends()
     */
    @GetMapping("/categoryTrends")
    @Operation(summary = "Get category trend analytics", description = "Provides insights into the trends of different recipe categories.")
    @ApiResponse(responseCode = "200", description = "Category trends successfully retrieved")
    public ResponseEntity<List<TrendAnalyticsDTO>> getCategoryTrends() {
        return ResponseEntity.ok(adminService.getCategoryTrends());
    }


    /**
     * Retrieves the ranking of chefs calculated using a Bayesian algorithm.
     * @return a {@link ResponseEntity} containing a list of five {@link ChefRankAnalyticsDTO} representing the Bayesian ranking of the top 5 chefs
     * @see AdminService#getBayesianRanking()
     */
    @GetMapping("/chefsRanking")
    @Operation(summary = "Get bayesian chef ranking", description = "Retrieves the ranking of chefs calculated using a Bayesian algorithm.")
    @ApiResponse(responseCode = "200", description = "Chef ranking successfully retrieved")
    public ResponseEntity<java.util.List<ChefRankAnalyticsDTO>> getChefRanking() {
        return ResponseEntity.ok(adminService.getBayesianRanking());
    }

}

