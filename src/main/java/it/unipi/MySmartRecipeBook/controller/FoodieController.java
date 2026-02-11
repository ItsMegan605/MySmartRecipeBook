package it.unipi.MySmartRecipeBook.controller;

import it.unipi.MySmartRecipeBook.dto.FoodieResponseDTO;
import it.unipi.MySmartRecipeBook.dto.UpdateFoodieDTO;
import it.unipi.MySmartRecipeBook.service.FoodieService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/foodies")
@PreAuthorize("hasRole('FOODIE')")
public class FoodieController {

    private final FoodieService foodieService;

    public FoodieController(FoodieService foodieService) {
        this.foodieService = foodieService;
    }

    /* ===== PROFILE ===== */

    @GetMapping("/me")
    public ResponseEntity<FoodieResponseDTO> getMe() {

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return ResponseEntity.ok(
                foodieService.getByUsername(username)
        );
    }

    @PatchMapping("/me")
    public ResponseEntity<FoodieResponseDTO> updateMe(
            @RequestBody @Valid UpdateFoodieDTO updates) {

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return ResponseEntity.ok(
                foodieService.updateFoodie(username, updates)
        );
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe() {

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        foodieService.deleteFoodie(username);

        return ResponseEntity.noContent().build();
    }

    /* ===== SAVED RECIPES ===== */

    @PostMapping("/me/saved-recipes/{recipeId}")
    public ResponseEntity<Void> saveRecipe(
            @PathVariable String recipeId) {

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        foodieService.saveRecipe(username, recipeId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me/saved-recipes/{recipeId}")
    public ResponseEntity<Void> removeSavedRecipe(
            @PathVariable String recipeId) {

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        foodieService.removeSavedRecipe(username, recipeId);

        return ResponseEntity.noContent().build();
    }
}
