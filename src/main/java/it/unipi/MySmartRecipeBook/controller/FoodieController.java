package it.unipi.MySmartRecipeBook.controller;

import it.unipi.MySmartRecipeBook.model.Foodie;
import it.unipi.MySmartRecipeBook.service.FoodieService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class FoodieController {

    private final FoodieService foodieService;

    public FoodieController(FoodieService foodieService) {
        this.foodieService = foodieService;
    }

    // Classe per mappare il login JSON
    public static class LoginRequest {
        public String username;
        public String password;
    }

    //la register va fatta a modo perchè dobbiamo metterci tutti i campi che vogliamo
    //non so se qui ci va la save oppure no

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Usiamo il service per cercare username e password
        Foodie foodie = foodieService.login(request.username, request.password);

        if (foodie != null) {
            return ResponseEntity.ok(foodie);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenziali Foodie errate");
    }
}