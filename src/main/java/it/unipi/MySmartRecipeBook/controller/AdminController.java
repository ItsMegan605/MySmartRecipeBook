package it.unipi.MySmartRecipeBook.controller;

import it.unipi.MySmartRecipeBook.service.AdminService;;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/approve/{id}")
    public void approveRecipe(@PathVariable("id") String recipeId) {
        adminService.saveRecipe(recipeId);
    }

    @DeleteMapping("/discard/{id}")
    public void discardRecipe(@PathVariable String recipeId) {
        adminService.discardRecipe(recipeId);
    }

}

