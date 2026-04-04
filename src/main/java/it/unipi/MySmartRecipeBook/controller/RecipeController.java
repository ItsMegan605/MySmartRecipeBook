package it.unipi.MySmartRecipeBook.controller;

import it.unipi.MySmartRecipeBook.dto.recipe.ShowRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.SliceRecipeDTO;
import it.unipi.MySmartRecipeBook.service.RecipeService;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * Recipe's Controller
 */
@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }
    

    /**
     * When we click on a recipe preview all the details must be shown
     * @param id user id
     * @see RecipeService#getRecipeById(String)
     * @return RespondeEntity ok message 
     */
    @GetMapping("/{id}")
    public ResponseEntity<ShowRecipeDTO> getRecipe (@PathVariable String id) {

        ShowRecipeDTO standardRecipeDTO = recipeService.getRecipeById(id);
        return ResponseEntity.ok(standardRecipeDTO);
    }
    

    /* Delete Reciope
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteRecipe (@PathVariable String id) {

        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }*/

    /**
     * Function to search a recipe by title in the home page (the research will be done searching sub-strings).
     * Five recipes at the time will be shown
     * @param title
     * @param pageNumber
     * @see RecipeService#getRecipeByTitle(String, int) 
     * @return RespondeEntity ok message 
     */
    @GetMapping("/search")
    public ResponseEntity<SliceRecipeDTO> getRecipeByTitle(@RequestParam String title, @RequestParam(defaultValue = "1") int pageNumber){

        SliceRecipeDTO recipes_list = recipeService.getRecipeByTitle(title, pageNumber);
        return ResponseEntity.ok(recipes_list);
    }

    /**
     * Home page for the recipes with the newest recipes uploaded
     * @param pageNumber
     * @see RecipeService#getNewestRecipe(int) 
     * @return RespondeEntity ok message 
     */
    @GetMapping("/homeRecipe")
    public ResponseEntity<SliceRecipeDTO> getHomeRecipe (@RequestParam(defaultValue = "1") int pageNumber){

        SliceRecipeDTO recipe_list = recipeService.getNewestRecipe(pageNumber);
        return ResponseEntity.ok(recipe_list);
    }

    /**
     * Function to order the user's saved recipes by a specific category
     * @param pageNumber
     * @param category
     * @see RecipeService#getByCategory(int, String) 
     * @return RespondeEntity ok message 
     */

    /*  */
    @GetMapping("/category")
    public ResponseEntity<SliceRecipeDTO> getRecipeByCategory (@RequestParam(defaultValue = "1") int pageNumber, @RequestParam String category){

        SliceRecipeDTO recipe_list = recipeService.getByCategory(pageNumber, category);
        return ResponseEntity.ok(recipe_list);
    }

    /**
     * Method to get reciped by chef
     * @param pageNumber
     * @param chefId
     * @see RecipeService#getChefRecipePage(int, String) 
     * @return RespondeEntity ok message 
     */

    @GetMapping("/chef")
    public ResponseEntity<SliceRecipeDTO> getChefRecipes (@RequestParam(defaultValue = "1") int pageNumber, @RequestParam String chefId){

        SliceRecipeDTO recipe_list = recipeService.getChefRecipePage(pageNumber, chefId);
        return ResponseEntity.ok(recipe_list);
    }
}
