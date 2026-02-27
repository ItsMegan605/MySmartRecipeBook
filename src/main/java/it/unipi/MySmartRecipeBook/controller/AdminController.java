package it.unipi.MySmartRecipeBook.controller;

import it.unipi.MySmartRecipeBook.dto.AnalyticsDTO;
import it.unipi.MySmartRecipeBook.service.AdminService;;
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

    @PostMapping("/approveChef/{id}")
    public ResponseEntity<String> approveChef(@PathVariable("id") String chefId) {
        adminService.approveChef(chefId);
        return ResponseEntity.ok("Chef succesfully added by admin");
    }

    @PostMapping("/discardChef/{id}")
    public ResponseEntity<String> discardChef(@PathVariable("id") String chefId) {
        adminService.declineChef(chefId);
        return ResponseEntity.ok("Chef declined by admin");
    }

    @GetMapping ("/monthlyFoodies")
    public ResponseEntity<List<AnalyticsDTO>> getMonthlyFoodies() {
        List<AnalyticsDTO> stats = adminService.getMonthlyFoodies();
        return ResponseEntity.ok(stats);
    }
}

