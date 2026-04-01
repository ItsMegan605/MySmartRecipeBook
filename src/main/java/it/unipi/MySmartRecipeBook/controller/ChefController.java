package it.unipi.MySmartRecipeBook.controller;

import it.unipi.MySmartRecipeBook.dto.recipe.SliceRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.users.RegistedUserInfoDTO;
import it.unipi.MySmartRecipeBook.dto.users.TopChefDTO;
import it.unipi.MySmartRecipeBook.dto.users.UpdateChefDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.ChefPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.CreateRecipeDTO;
import it.unipi.MySmartRecipeBook.service.ChefService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

/**
 * Chef's controller
 */

@RestController
@RequestMapping("/api/chefs")
@PreAuthorize("hasRole('CHEF')")
public class ChefController {

    private final ChefService chefService;

    public ChefController(ChefService chefService) {
        this.chefService = chefService;
    }



    /**
     * Method Retrieve chef's information
     * @see ChefService#getByUsername(String)
     * @return ResponseEntity ok message
     */

    @GetMapping("/info")
    public ResponseEntity<RegistedUserInfoDTO> getInformations() {

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return ResponseEntity.ok(chefService.getByUsername(username));
    }



    /**
     * Change chef's information
     * @param updates
     * @see ChefService#updateChef(UpdateChefDTO)
     * @return ResponseEntity ok message
     */
    @PostMapping("/changeInfo")
    public ResponseEntity<RegistedUserInfoDTO> updateInformation (@Valid @RequestBody UpdateChefDTO updates){

        if(updates.getBirthdate() != null && Period.between(updates.getBirthdate(), LocalDate.now()).getYears() < 15){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You must be at least 15");
        }
        return ResponseEntity.ok(chefService.updateChef(updates));
    }


    /**
     * Delete chef's profile
     * @see ChefService#deleteChef(String)
     * @return ResponseEntity with message
     */

    @DeleteMapping("/deleteProfile")
    public ResponseEntity<String> deleteProfile() {

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        chefService.deleteChef(username);
        return ResponseEntity.ok("Profile successfully deleted. We are sorry to see you leaving");
    }



    /**
     * Method to handle a new recipe
     * @param dto
     * @see ChefService#createRecipe(CreateRecipeDTO)
     * @return ResponseEntity ok message
     */

    @PostMapping("/addNewRecipe")
    public ResponseEntity<ChefPreviewRecipeDTO> saveRecipe (@Valid @RequestBody CreateRecipeDTO dto){

        ChefPreviewRecipeDTO recipe = chefService.createRecipe(dto);
        return ResponseEntity.ok(recipe);
    }



    /**
     * Remove a recipe that is waiting to be confirmed
     * @param recipeId
     * @see ChefService#removeRecipe(String)
     * @return ResponseEntity ok message
     */

    @DeleteMapping("/removeWaiting/{id}")
    public ResponseEntity<String> removeRecipe (@PathVariable("id") String recipeId){

        chefService.removeRecipe(recipeId);
        return ResponseEntity.ok("Recipe succesfully removed");
    }


    /**
     * Method to delete a recipe that already exists
     * @param recipeId
     * @see ChefService#deleteRecipe(String)
     * @return ResponseEntity with message
     */

    @DeleteMapping("/deleteRecipe/{id}")
    public ResponseEntity<String> deleteRecipe (@PathVariable("id") String recipeId){

        chefService.deleteRecipe(recipeId);
        return ResponseEntity.ok("Recipe succesfully deleted");
    }



    /**
     * Show a recipe to a chef
     * @param page
     * @see ChefService#showRecipes(int)
     * @return ResponseEntity ok message
     */

    @GetMapping("/show/{page}")
    public ResponseEntity<SliceRecipeDTO> showRecipe (@PathVariable("page") int page){
        SliceRecipeDTO recipeList = chefService.showRecipes(page);
        return ResponseEntity.ok(recipeList);
    }

    /**
     * Show to the chef his/her popular recipes
     * @param pageNumber
     * @see ChefService#showPopularRecipes(int)
     * @return ResponseEntity ok message
     */

    @GetMapping("/popular/{pageNumber}")
    public ResponseEntity<SliceRecipeDTO> popularRecipe (@PathVariable("pageNumber") int pageNumber){
        SliceRecipeDTO recipeList = chefService.showPopularRecipes(pageNumber);
        return ResponseEntity.ok(recipeList);
    }

    /**
     * Show top 3 chefs per Category
     * @see ChefService#getTopChef()
     * @return ResponseEntity ok message
     */


    @GetMapping("/getTopChef")
    public ResponseEntity<List<TopChefDTO>> getTopChef() {
        List<TopChefDTO> topChefs = chefService.getTopChef();
        return ResponseEntity.ok(topChefs);
    }

}
