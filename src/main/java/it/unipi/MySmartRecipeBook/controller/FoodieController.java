package it.unipi.MySmartRecipeBook.controller;

import it.unipi.MySmartRecipeBook.dto.foodie.StandardFoodieDTO;
import it.unipi.MySmartRecipeBook.dto.foodie.UpdateStandardFoodieDTO;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;
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


    @GetMapping("/getMe")
    public ResponseEntity<StandardFoodieDTO> getMe(){

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        return ResponseEntity.ok(foodieService.getByUsername(username));
    }

    @PatchMapping("/updateMe")
    public ResponseEntity<StandardFoodieDTO> updateMe (@RequestBody @Valid UpdateStandardFoodieDTO updates){

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        return ResponseEntity.ok(foodieService.updateFoodie(username, updates));
    }

    @DeleteMapping("/deleteMe")
    public ResponseEntity<Void> deleteMe() {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        foodieService.deleteFoodie(username);

        return ResponseEntity.noContent().build();
    }

//functions for saved recipes
    @PostMapping("/me/saved-recipes/{recipeId}")
    public ResponseEntity<Void> saveRecipe (@PathVariable String recipeId) {

        UserPrincipal foodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        String foodieID = foodie.getId();
        foodieService.saveRecipe(foodieID, recipeId);

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
