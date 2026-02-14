package it.unipi.MySmartRecipeBook.controller;

import it.unipi.MySmartRecipeBook.model.Recipe;
import it.unipi.MySmartRecipeBook.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import it.unipi.MySmartRecipeBook.service.AdminService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    //mettere poi service analytics
    //l'admin possiamo fare che può anche lui cancellare le ricette per dire
    //e poi farà le cose delle analytics che faremo poi

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @DeleteMapping("/delete/recipe/{recipeId}")
    public ResponseEntity<String> deleteRecipe(@PathVariable String recipeId) {
        // Chiama il metodo del service che si occuperà di ripulire Mongo, Neo4j e Redis
        adminService.deleteRecipe(recipeId);

        return ResponseEntity.ok("Ricetta eliminata con successo");
    }
    //fare funzione per approvare gli chef
    //alcune che avevamo detto: ingredienti più usati
    //avg numero di ricette salvate di ogni chef in certo tempo
    //forse conviene ridurli?
    //The Admin can view the average number of Foodies
    // that register to the application every month

}
