package it.unipi.MySmartRecipeBook.controller;

import it.unipi.MySmartRecipeBook.model.Recipe;
import it.unipi.MySmartRecipeBook.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/approve/{id}")
    public void approveRecipe(@PathVariable String recipeId) {

        adminService.saveRecipe(recipeId);
    }

}

