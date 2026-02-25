package it.unipi.MySmartRecipeBook.controller;

import it.unipi.MySmartRecipeBook.dto.users.RegistedUserInfoDTO;
import it.unipi.MySmartRecipeBook.dto.users.UpdateFoodieDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.UserPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import it.unipi.MySmartRecipeBook.service.FoodieService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Slice;
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

    /*--------------- Retrieve foodie's informations ----------------*/

    @GetMapping("/info")
    public ResponseEntity<RegistedUserInfoDTO> getInfo(){

        return ResponseEntity.ok(foodieService.getById());
    }


    /*--------------- Change foodie's informations ----------------*/

    @PatchMapping("/changeInfo")
    public ResponseEntity<RegistedUserInfoDTO> changeInfo (@RequestBody @Valid UpdateFoodieDTO updates){

        return ResponseEntity.ok(foodieService.updateFoodie(updates));
    }


    /*------------------ Delete foodie's Profile -------------------*/

    @DeleteMapping("/deleteProfile")
    public ResponseEntity<String> deleteProfile() {

        foodieService.deleteFoodie();
        return ResponseEntity.ok("Foodie has been succesfully deleted");
    }


    /*------------ Add a recipe to foodie's favourites  -------------*/

    @PostMapping("/addFavourite/{recipeId}")
    public ResponseEntity<String> saveRecipe (@PathVariable String recipeId) {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        foodieService.saveRecipe(authFoodie.getId(), recipeId);
        return ResponseEntity.ok("Recipe has been succesfully add to favourites");
    }


    /*------------ Remove a recipe from foodie's favourites  -------------*/

    @DeleteMapping("/removeFavourite/{recipeId}")
    public ResponseEntity<String> removeSavedRecipe(@PathVariable String recipeId) {

        foodieService.removeSavedRecipe(recipeId);
        return ResponseEntity.ok("Recipe has been succesfully removed from favourites");
    }


    /*------------ Order favourites recipes by filter -------------*/
    @GetMapping("/getRecipe/{category}/{numPage}")
    public ResponseEntity<Slice<UserPreviewRecipeDTO>> getRecipeByCategory (@PathVariable String category,
                                                                            @PathVariable int numPage) {
        Slice<UserPreviewRecipeDTO> recipeList = foodieService.getRecipeByCategory(category, numPage);
        return ResponseEntity.ok(recipeList);
    }
}
