package it.unipi.MySmartRecipeBook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.MySmartRecipeBook.dto.recipe.*;
import it.unipi.MySmartRecipeBook.dto.users.RegisteredUserInfoDTO;
import it.unipi.MySmartRecipeBook.dto.users.UpdateChefDTO;
import it.unipi.MySmartRecipeBook.service.ChefService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Period;

/**
 * Chef's controller
 */
@RestController
@RequestMapping("/api/chefs")
@PreAuthorize("hasRole('CHEF')")
@Tag(name = "Chef", description = "Endpoints for managing Chef profiles and their recipes")
public class ChefController {

    private final ChefService chefService;
    public ChefController(ChefService chefService) {

        this.chefService = chefService;
    }


    /**
     * Retrieves the personal information of the currently authenticated chef.
     * @return a {@link ResponseEntity} containing a {@link RegisteredUserInfoDTO} with the chef's personal information
     * @see ChefService#getByUsername()
     */
    @GetMapping("/info")
    @Operation(summary = "Retrieve the chef's personal information")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<RegisteredUserInfoDTO> getInformation() {

        return ResponseEntity.ok(chefService.getByUsername());
    }


    /**
     * Changes the personal information of the currently authenticated chef.
     * @param updates a {@link UpdateChefDTO} containing the chef's personal information to update
     * @return a {@link ResponseEntity} containing a {@link RegisteredUserInfoDTO} with the updated chef's personal information
     * @throws IllegalArgumentException if the updated birthdate results in an age under 15
     * @see ChefService#updateChef(UpdateChefDTO)
     */
    @PostMapping("/changeInfo")
    @Operation(summary = "Change chef's information")
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400")
    })
    public ResponseEntity<RegisteredUserInfoDTO> updateInformation (@Valid @RequestBody UpdateChefDTO updates){

        if(updates.getBirthdate() != null && Period.between(updates.getBirthdate(), LocalDate.now()).getYears() < 15){
            throw new IllegalArgumentException("You must be at least 15 years old to use this service");        }
        return ResponseEntity.ok(chefService.updateChef(updates));
    }


    /**
     * Deletes the profile of the authenticated chef.
     * @return a {@link ResponseEntity} with a success message confirming the deletion
     * @see ChefService#deleteChef()
     */
    @DeleteMapping("/deleteProfile")
    @Operation(summary = "Delete chef's profile")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<String> deleteProfile() {

        chefService.deleteChef();
        return ResponseEntity.ok("Profile successfully deleted. We are sorry to see you leave");
    }


    /**
     * Creates a new recipe for the currently authenticated chef.
     * @param recipeDTO a {@link CreateRecipeDTO} containing all the mandatory fields provided by the chef
     * @return a {@link ResponseEntity} containing a {@link PendingRecipeChefDTO} with the details of the newly created pending recipe
     * @see ChefService#createRecipe(CreateRecipeDTO)
     */
    @PostMapping("/addNewRecipe")
    @Operation(summary = "Submit a new recipe")
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400")
    })
    public ResponseEntity<PendingRecipeChefDTO> saveRecipe (@Valid @RequestBody CreateRecipeDTO recipeDTO){

        PendingRecipeChefDTO recipe = chefService.createRecipe(recipeDTO);
        return ResponseEntity.ok(recipe);
    }

    /**
     * Retrieves a paginated list of the chef's recipes that are currently waiting for approval.
     * @param pageNumber the requested page number
     * @return a {@link ResponseEntity} containing a list of {@link PendingRecipeChefDTO} with the paginated preview of the pending recipes
     * @see ChefService#showPendingRecipes(int)
     */
    @GetMapping("/showWaiting/{pageNumber}")
    @Operation(summary = "Show chef's pending recipes")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SliceRecipeDTO<PendingRecipeChefDTO>> showPendingRecipes (@PathVariable int pageNumber){
        SliceRecipeDTO<PendingRecipeChefDTO> recipeList = chefService.showPendingRecipes(pageNumber);
        return ResponseEntity.ok(recipeList);
    }


    /**
     * Removes a pending recipe that is waiting for the admin approval.
     * @param recipeId the unique identifier of the target recipe
     * @return a {@link ResponseEntity} with a success message confirming the deletion
     * @see ChefService#removeRecipe(String)
     */
    @DeleteMapping("/removeWaiting/{id}")
    @Operation(summary = "Remove a pending recipe")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<String> removeRecipe (@PathVariable("id") String recipeId){

        chefService.removeRecipe(recipeId);
        return ResponseEntity.ok("Recipe successfully removed");
    }


    /**
     * Deletes a recipe that has already been approved.
     * @param recipeId the unique identifier of the target recipe
     * @return a {@link ResponseEntity} with a success message confirming the deletion
     * @see ChefService#deleteRecipe(String)
     */
    @DeleteMapping("/deleteRecipe/{id}")
    @Operation(summary = "Delete an already approved recipe")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<String> deleteRecipe (@PathVariable("id") String recipeId){

        chefService.deleteRecipe(recipeId);
        return ResponseEntity.ok("Recipe successfully deleted");
    }


    /**
     * Retrieves a paginated list of the preview of the chef's approved recipes.
     * @param pageNumber the requested page number
     * @return a {@link ResponseEntity} containing a list of {@link ChefPreviewRecipeDTO} with the paginated preview of the chef's recipes
     * @see ChefService#showRecipes(int)
     */
    @GetMapping("/show/{pageNumber}")
    @Operation(summary = "Show published recipes")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SliceRecipeDTO<ChefPreviewRecipeDTO>> showRecipe (@PathVariable int pageNumber){
        SliceRecipeDTO<ChefPreviewRecipeDTO> recipeList = chefService.showRecipes(pageNumber);
        return ResponseEntity.ok(recipeList);
    }


    /**
     * Retrieves a paginated list of the preview of the chef's popular recipes.
     * @param pageNumber - page number
     * @return a {@link ResponseEntity} containing a list of {@link ChefPreviewRecipeDTO} with the paginated preview of the chef's popular recipes
     * @see ChefService#showPopularRecipes(int)
     */
    @GetMapping("/popular/{pageNumber}")
    @Operation(summary = "Show popular recipes")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SliceRecipeDTO<ChefPreviewRecipeDTO>> popularRecipe (@PathVariable int pageNumber){
        SliceRecipeDTO<ChefPreviewRecipeDTO> recipeList = chefService.showPopularRecipes(pageNumber);
        return ResponseEntity.ok(recipeList);
    }


    /**
     * Retrieves the detailed information about a specific pending recipe.
     * @param recipeId the unique identifier of the pending recipe
     * @return a {@link ResponseEntity} containing a {@link ShowRecipeDTO} with the details of the specific pending recipe
     * @see ChefService#getRecipeDetails(String)
     */
    @GetMapping("/details/pending/{recipeId}")
    @Operation(summary = "Get pending recipe details")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<ShowRecipeDTO> getRecipeDetails (@PathVariable String recipeId){
        ShowRecipeDTO recipe = chefService.getRecipeDetails(recipeId);
        return ResponseEntity.ok(recipe);
    }
}
