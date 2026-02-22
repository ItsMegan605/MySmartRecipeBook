package it.unipi.MySmartRecipeBook.controller;
import it.unipi.MySmartRecipeBook.dto.recipe.ChefPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.ShowRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.UserPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.service.RecipeService;

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


    /* When we click on a recipe preview all the details must be shown*/
    @GetMapping("/{id}")
    public ResponseEntity<ShowRecipeDTO> getRecipe (@PathVariable String id) {

        ShowRecipeDTO standardRecipeDTO = recipeService.getRecipeById(id);
        return ResponseEntity.ok(standardRecipeDTO);
    }

    /* Delete Reciope */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteRecipe (@PathVariable String id) {

        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }

    /* Function to search a recipe by title in the home page (the research will be done searching sub-strings).
    Five recipes at the time will be shown */
    @GetMapping("/search")
    public ResponseEntity<List<UserPreviewRecipeDTO>> getRecipeByTitle(@RequestParam String title, @RequestParam(defaultValue = "1") Integer pageNumber){

        List<UserPreviewRecipeDTO> recipes_list = recipeService.getRecipeByTitle(title, pageNumber);
        return ResponseEntity.ok(recipes_list);
    }

    @GetMapping("/homeRecipe")
    public ResponseEntity<List<UserPreviewRecipeDTO>> getHomeRecipe (@RequestParam(defaultValue = "1") Integer pageNumber){

        List<UserPreviewRecipeDTO> recipe_list = recipeService.getNewestRecipe(pageNumber);
        return ResponseEntity.ok(recipe_list);
    }

    /* Function to order the user saved recipes by specifing a category */
    @GetMapping("/category")
    public ResponseEntity<List<UserPreviewRecipeDTO>> getRecipeByCategory (@RequestParam(defaultValue = "1") Integer pageNumber, @RequestParam String category){

        List<UserPreviewRecipeDTO> recipe_list = recipeService.getByCategory(pageNumber, category);
        return ResponseEntity.ok(recipe_list);
    }

    @GetMapping("/chef")
    public ResponseEntity<List<ChefPreviewRecipeDTO>> getChefRecipes (@RequestParam(defaultValue = "1") Integer pageNumber, @RequestParam String chefName){

        List<ChefPreviewRecipeDTO> recipe_list = recipeService.getChefRecipePage(pageNumber, chefName);
        return ResponseEntity.ok(recipe_list);
    }
}
