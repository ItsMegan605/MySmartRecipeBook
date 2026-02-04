package it.unipi.MySmartRecipeBook.controller;

import it.unipi.MySmartRecipeBook.model.Foodie;
import it.unipi.MySmartRecipeBook.service.FoodieService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

package it.unipi.MySmartRecipeBook.controller;

import it.unipi.MySmartRecipeBook.dto.CreateFoodieDTO;
import it.unipi.MySmartRecipeBook.dto.UpdateFoodieDTO;
import it.unipi.MySmartRecipeBook.dto.FoodieResponseDTO;
import it.unipi.MySmartRecipeBook.model.Foodie;
import it.unipi.MySmartRecipeBook.service.FoodieService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/foodies")
public class FoodieController {

    private final FoodieService foodieService;

    public FoodieController(FoodieService foodieService) {
        this.foodieService = foodieService;
    }

    /* =========================
       CREATE FOODIE
       ========================= */
    @PostMapping
    public ResponseEntity<FoodieResponseDTO> createFoodie(
            @Valid @RequestBody CreateFoodieDTO dto) {

        Foodie foodie = foodieService.createFoodie(dto);

        FoodieResponseDTO response = new FoodieResponseDTO(
                foodie.getUsername(),
                foodie.getName(),
                foodie.getSurname(),
                foodie.getEmail()
        );

        return ResponseEntity.ok(response);
    }

    /* =========================
       GET FOODIE
       ========================= */
    @GetMapping("/{username}")
    public ResponseEntity<FoodieResponseDTO> getFoodie(
            @PathVariable String username) {

        return foodieService.getFoodieByUsername(username)
                .map(foodie -> new FoodieResponseDTO(
                        foodie.getUsername(),
                        foodie.getName(),
                        foodie.getSurname(),
                        foodie.getEmail()
                ))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /* =========================
       UPDATE FOODIE
       ========================= */
    @PutMapping("/{username}")
    public ResponseEntity<FoodieResponseDTO> updateFoodie(
            @PathVariable String username,
            @Valid @RequestBody UpdateFoodieDTO dto) {

        Foodie foodie = foodieService.updateFoodie(username, dto);

        return ResponseEntity.ok(
                new FoodieResponseDTO(
                        foodie.getUsername(),
                        foodie.getName(),
                        foodie.getSurname(),
                        foodie.getEmail()
                )
        );
    }

    /* =========================
       DELETE FOODIE
       ========================= */
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteFoodie(
            @PathVariable String username) {

        foodieService.deleteFoodie(username);
        return ResponseEntity.noContent().build();
    }

    /* =========================
       SAVED RECIPES (MongoDB)
       ========================= */
    @PostMapping("/{username}/saved-recipes/{recipeId}")
    public ResponseEntity<Void> saveRecipe(
            @PathVariable String username,
            @PathVariable String recipeId) {

        foodieService.saveRecipe(username, recipeId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{username}/saved-recipes/{recipeId}")
    public ResponseEntity<Void> removeSavedRecipe(
            @PathVariable String username,
            @PathVariable String recipeId) {

        foodieService.removeSavedRecipe(username, recipeId);
        return ResponseEntity.noContent().build();
    }
}


/*
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
 */