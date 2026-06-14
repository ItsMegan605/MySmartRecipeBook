package it.unipi.MySmartRecipeBook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.MySmartRecipeBook.service.IngredientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * Controller for ingredients endpoints
 */
@RestController
@RequestMapping("/api/ingredients")
@Tag(name = "Ingredients", description = "Endpoints for managing ingredients allowed in the service")
public class IngredientController {

    private final IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {

        this.ingredientService = ingredientService;
    }


    /**
     * Retrieves the complete set of allowed ingredients available in the application.
     * @return a {@link ResponseEntity} containing a {@link Set} of strings representing the allowed ingredients
     * @see IngredientService#getAllAllowedIngredients()
     */
    @GetMapping("/allowedIngredients")
    @Operation(summary = "Allowed ingredients", description = "Retrieves the complete list of allowed ingredients available in the application")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<Set<String>> getAllAllowedIngredients() {
        Set<String> allowedIngredients = ingredientService.getAllAllowedIngredients();
        return ResponseEntity.ok(allowedIngredients);
    }
}