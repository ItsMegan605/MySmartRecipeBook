package it.unipi.MySmartRecipeBook.controller;
import it.unipi.MySmartRecipeBook.dto.recipe.ChefPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.ShowRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.SliceRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.UserPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.service.RecipeService;

import org.springframework.data.domain.Slice;
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
    // Quando clicco su una ricetta dello smartFridge - attenzione mettere che è un metodo che si può
    // fare solo se si è autenticati
    @GetMapping("/{id}/{fridge}")
    public ResponseEntity<ShowRecipeDTO> getRecipe (@PathVariable String id, @PathVariable Boolean fridge) {

        ShowRecipeDTO standardRecipeDTO = recipeService.getRecipeById(id, fridge);
        return ResponseEntity.ok(standardRecipeDTO);
    }

    /* Delete Reciope
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteRecipe (@PathVariable String id) {

        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }*/

    /* Function to search a recipe by title in the home page (the research will be done searching sub-strings).
    Five recipes at the time will be shown */
    @GetMapping("/search")
    public ResponseEntity<SliceRecipeDTO> getRecipeByTitle(@RequestParam String title, @RequestParam(defaultValue = "1") int pageNumber){

        SliceRecipeDTO recipes_list = recipeService.getRecipeByTitle(title, pageNumber);
        return ResponseEntity.ok(recipes_list);
    }

    @GetMapping("/homeRecipe")
    public ResponseEntity<SliceRecipeDTO> getHomeRecipe (@RequestParam(defaultValue = "1") int pageNumber){

        SliceRecipeDTO recipe_list = recipeService.getNewestRecipe(pageNumber);
        return ResponseEntity.ok(recipe_list);
    }

    /* Function to order the user saved recipes by specifing a category */
    @GetMapping("/category")
    public ResponseEntity<SliceRecipeDTO> getRecipeByCategory (@RequestParam(defaultValue = "1") int pageNumber, @RequestParam String category){

        SliceRecipeDTO recipe_list = recipeService.getByCategory(pageNumber, category);
        return ResponseEntity.ok(recipe_list);
    }

    @GetMapping("/chef")
    public ResponseEntity<SliceRecipeDTO> getChefRecipes (@RequestParam(defaultValue = "1") int pageNumber, @RequestParam String chefName){

        SliceRecipeDTO recipe_list = recipeService.getChefRecipePage(pageNumber, chefName);
        return ResponseEntity.ok(recipe_list);
    }
}
