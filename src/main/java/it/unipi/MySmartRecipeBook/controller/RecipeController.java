package it.unipi.MySmartRecipeBook.controller;
import it.unipi.MySmartRecipeBook.dto.CreateRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.UpdateRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.RecipeResponseDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.RecipeMongo;
import it.unipi.MySmartRecipeBook.service.RecipeMongoService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeMongoService recipeService;

    public RecipeController(RecipeMongoService recipeService) {
        this.recipeService = recipeService;
    }

    /* =========================
       CREATE RECIPE
       ========================= */
    @PostMapping
    public ResponseEntity<RecipeResponseDTO> createRecipe(
            @Valid @RequestBody CreateRecipeDTO dto) {

        RecipeMongo recipe = recipeService.createRecipe(dto);

        return ResponseEntity.ok(
                new RecipeResponseDTO(
                        recipe.getId(),
                        recipe.getTitle(),
                        recipe.getCategory(),
                        recipe.getPrepTime(),
                        recipe.getDifficulty(),
                        recipe.getPhotoURL(),
                        recipe.getIngredients(),
                        recipe.getChefUsername()
                )
        );
    }

    /* =========================
       GET RECIPE
       ========================= */
    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponseDTO> getRecipe(
            @PathVariable String id) {

        return recipeService.getRecipeById(id)
                .map(recipe -> new RecipeResponseDTO(
                        recipe.getId(),
                        recipe.getTitle(),
                        recipe.getCategory(),
                        recipe.getPrepTime(),
                        recipe.getDifficulty(),
                        recipe.getPhotoURL(),
                        recipe.getIngredients(),
                        recipe.getChefUsername()
                ))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /* =========================
       UPDATE RECIPE
       ========================= */
    @PutMapping("/{id}")
    public ResponseEntity<RecipeResponseDTO> updateRecipe(
            @PathVariable String id,
            @Valid @RequestBody UpdateRecipeDTO dto) {

        RecipeMongo recipe = recipeService.updateRecipe(id, dto);

        return ResponseEntity.ok(
                new RecipeResponseDTO(
                        recipe.getId(),
                        recipe.getTitle(),
                        recipe.getCategory(),
                        recipe.getPrepTime(),
                        recipe.getDifficulty(),
                        recipe.getPhotoURL(),
                        recipe.getIngredients(),
                        recipe.getChefUsername()
                )
        );
    }

    /* =========================
       DELETE RECIPE
       ========================= */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(
            @PathVariable String id) {

        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }

    /* =========================
       GET ALL RECIPES
       ========================= */
    @GetMapping
    public ResponseEntity<List<RecipeMongo>> getAllRecipes() {
        return ResponseEntity.ok(recipeService.getAllRecipes());
    }
}
