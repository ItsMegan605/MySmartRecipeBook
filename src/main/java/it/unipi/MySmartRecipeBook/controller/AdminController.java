package it.unipi.MySmartRecipeBook.controller;

import it.unipi.MySmartRecipeBook.dto.*;
import it.unipi.MySmartRecipeBook.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /*------------------- Approve a pending recipe  --------------------*/

    @PostMapping("/approve/{id}")
    public ResponseEntity<String> approveRecipe(@PathVariable("id") String recipeId) {
        adminService.saveRecipe(recipeId);
        return ResponseEntity.ok("Recipe approved");
    }

    /*------------------- Discard a pending recipe  --------------------*/

    @DeleteMapping("/discard/{id}")
    public ResponseEntity<String> discardRecipe(@PathVariable("id") String recipeId) {
        adminService.discardRecipe(recipeId);
        return ResponseEntity.ok("Recipe succesfully discarded");
    }

    @PostMapping("/approveChef/{username}")
    public ResponseEntity<String> approveChef(@PathVariable("username") String chefUsername) {
        adminService.approveChef(chefUsername);
        return ResponseEntity.ok("Chef succesfully added by admin");
    }

    @PostMapping("/discardChef/{username}")
    public ResponseEntity<String> discardChef(@PathVariable("username") String chefUsername) {
        adminService.declineChef(chefUsername);
        return ResponseEntity.ok("Chef declined by admin");
    }

    @GetMapping ("/monthlyFoodies")
    public ResponseEntity<List<YearAnalyticsDTO>> getMonthlyFoodies() {
        List<YearAnalyticsDTO> stats = adminService.getMonthlyFoodies();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/popularIngredients") //TODO forse da togliere
    public ResponseEntity<List<PopularIngredientsDTO>> getPopularIngredients() {
        List<PopularIngredientsDTO> ingredients = adminService.getPopularIngredients();
        return ResponseEntity.ok(ingredients);
    }

    @GetMapping("/categoryTrends")
    public ResponseEntity<List<TrendAnalyticsDTO>> getCategoryTrends() {
        return ResponseEntity.ok(adminService.getCategoryTrends());
    }
/*
    @GetMapping("/leastUsedIngredients")
    public ResponseEntity<List<UsedIngredientsDTO>> getLeastUsedIngredients() {
        List<UsedIngredientsDTO> rareIngredients = adminService.getLeastUsedIngredients();
        return ResponseEntity.ok(rareIngredients);
    } */
}

