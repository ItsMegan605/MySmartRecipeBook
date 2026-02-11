package it.unipi.MySmartRecipeBook.controller;
import it.unipi.MySmartRecipeBook.dto.recipe.ChefPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.RecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.UserPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.service.RecipeService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    /* Dopo aver compilato il form, lo chef schiaccia save per salvare la ricetta*/
    @PostMapping("/addNewRecipe")
    public ResponseEntity<ChefPreviewRecipeDTO> saveRecipe(
            @Valid @RequestBody RecipeDTO dto) {

        // creazione elemento ricetta che viene inserito nel DB di mongo
        ChefPreviewRecipeDTO recipe = recipeService.createRecipe(dto);
        return ResponseEntity.ok(recipe);
        // forse qui basta anche solo l'esito del salvataggio
    }

    /* Restituisce il contenuto della ricetta, leggendola direttamente dal DB*/
    @GetMapping("/{id}")
    public ResponseEntity<RecipeDTO> getRecipe(
            @PathVariable String id) {

        RecipeDTO recipeDTO = recipeService.getRecipeById(id);
        return ResponseEntity.ok(recipeDTO);
    }

    /* Eliminazione ricetta */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteRecipe(
            @PathVariable String id) {

        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }

    /* Function to search a recipe by title in the home page (the research will be done searching sub-strings)*/
    @GetMapping("/search")
    public ResponseEntity<List<RecipeDTO>> getRecipeByTitle(@RequestParam String title){
        List<RecipeDTO> recipes_list = recipeService.getRecipeByTitle(title);

        return ResponseEntity.ok(recipes_list);
    }

    /* Function to show the latest updated recipes on the home page (all users - even the unregistered ones - can
    see this first page). In particular, if a user press the button relative to a specific page (for example 4th page),
    this function will be called */
    @GetMapping("load/{pageNumber}")
    public ResponseEntity<List<UserPreviewRecipeDTO>> getRecipesNewPage(@PathVariable Integer pageNumber){

        List<UserPreviewRecipeDTO> recipe_list = recipeService.getRecipePage(pageNumber, 3);
        return ResponseEntity.ok(recipe_list);
    }

    /* Function to order the user saved recipes by specifing a category*/
    @GetMapping("load/user/{pageNumber}/{filter}")
    public ResponseEntity<List<UserPreviewRecipeDTO>> getUserRecipes (@PathVariable Integer pageNumber, String filter){

        List<UserPreviewRecipeDTO> recipe_list = recipeService.getUserRecipePage(pageNumber, 3, filter);
        return ResponseEntity.ok(recipe_list);
    }

    @GetMapping("load/chef/{pageNumber}/{filter}")
    public ResponseEntity<List<ChefPreviewRecipeDTO>> getChefRecipes (@PathVariable Integer pageNumber, String filter){

        List<ChefPreviewRecipeDTO> recipe_list = recipeService.getChefRecipePage(pageNumber, 3, filter);
        return ResponseEntity.ok(recipe_list);
    }
}
