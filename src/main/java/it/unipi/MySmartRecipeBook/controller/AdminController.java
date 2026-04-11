package it.unipi.MySmartRecipeBook.controller;

import it.unipi.MySmartRecipeBook.dto.*;
import it.unipi.MySmartRecipeBook.dto.recipe.SliceRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.users.PendingChefDTO;
import it.unipi.MySmartRecipeBook.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Post Method to approve a pending recipe
     * @param recipeId
     * @see AdminService#saveRecipe(String)
     * @return ResponseEntity with approval message
     */
    @PostMapping("/approve/{id}")
    public ResponseEntity<String> approveRecipe(@PathVariable("id") String recipeId) {
        adminService.saveRecipe(recipeId);
        return ResponseEntity.ok("Recipe approved");
    }


    /**
     * Delete Method to discard a pending recipe
     * @param recipeId
     * @see AdminService#discardRecipe(String)
     * @return ResponseEntity with  message
     */
    @DeleteMapping("/discard/{id}")
    public ResponseEntity<String> discardRecipe(@PathVariable("id") String recipeId) {
        adminService.discardRecipe(recipeId);
        return ResponseEntity.ok("Recipe successfully discarded");
    }

    /**
     * Post method to approve a chef
     * @param chefUsername
     * @see AdminService#approveChef(String) 
     * @return ResponseEntity with  message
     */
    @PostMapping("/approveChef/{username}")
    public ResponseEntity<String> approveChef(@PathVariable("username") String chefUsername) {
        adminService.approveChef(chefUsername);
        return ResponseEntity.ok("Chef successfully added by admin");
    }

    /**
     * Post method to discard a chef's request to register
     * @param chefUsername
     * @see AdminService#declineChef(String)
     * @return ResponseEntity with  message
     */
    @PostMapping("/discardChef/{username}")
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
    public ResponseEntity<SliceRecipeDTO> showRecipe (@PathVariable("page") int page){
        SliceRecipeDTO recipeList = adminService.showPendingRecipes(page);
        return ResponseEntity.ok(recipeList);
    }

    /**
     * Get method for a paginated list of chef registration requests that are pending admin approval.
     * @param page the page number to retrieve
     * @see AdminService#showPendingChefs(int)
     * @return a ResponseEntity containing a slice of pending chef profiles
     */
    @GetMapping("/showChefs/{page}")
    public ResponseEntity<SliceRecipeDTO> showChefs (@PathVariable("page") int page){
        SliceRecipeDTO chefList = adminService.showPendingChefs(page);
        return ResponseEntity.ok(chefList);
    }

    /**
     * Get method to get the number of users that registered in past months
     * @return
     * @see AdminService#getMonthlyFoodies()
     * @return ResponseEntity with ok message
     */
    @GetMapping ("/monthlyFoodies")
    public ResponseEntity<List<YearAnalyticsDTO>> getMonthlyFoodies() {
        List<YearAnalyticsDTO> stats = adminService.getMonthlyFoodies();
        return ResponseEntity.ok(stats);
    }

    /**
     * Method to see the most popular ingredients used by a chef
     * @see AdminService#getPopularIngredients()
     * @return ResponseEntity with ok message
     *
     */
    @GetMapping("/popularIngredients") //TODO forse da togliere
    public ResponseEntity<List<PopularIngredientsDTO>> getPopularIngredients() {
        List<PopularIngredientsDTO> ingredients = adminService.getPopularIngredients();
        return ResponseEntity.ok(ingredients);
    }

    /**
     * Get method to see the trending of the different categories
     * @see AdminService#getCategoryTrends()
     * @return ResponseEntity with ok message
     *
     */
    @GetMapping("/categoryTrends")
    public ResponseEntity<List<TrendAnalyticsDTO>> getCategoryTrends() {
        return ResponseEntity.ok(adminService.getCategoryTrends());
    }
}

