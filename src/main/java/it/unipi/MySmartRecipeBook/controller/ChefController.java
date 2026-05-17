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
import org.springframework.security.core.context.SecurityContextHolder;
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
     * Method to Retrieve the chef's information
     * @see ChefService#getByUsername(String)
     * @return ResponseEntity ok message
     */
    @GetMapping("/info")
    @Operation(summary = "Retrieve the chef's information")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<RegisteredUserInfoDTO> getInformation() {

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return ResponseEntity.ok(chefService.getByUsername(username));
    }

    /**
     * Change chef's information
     * @param updates - update chef info
     * @see ChefService#updateChef(UpdateChefDTO)
     * @return ResponseEntity ok message
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
     * Delete chef's profile
     * @see ChefService#deleteChef(String)
     * @return ResponseEntity with message
     */
    @DeleteMapping("/deleteProfile")
    @Operation(summary = "Delete chef's profile")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<String> deleteProfile() {

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        chefService.deleteChef(username);
        return ResponseEntity.ok("Profile successfully deleted. We are sorry to see you leaving");
    }

    /**
     * Method to handle a new recipe
     * @param dto - recipe DTO
     * @see ChefService#createRecipe(CreateRecipeDTO)
     * @return ResponseEntity ok message
     */
    @PostMapping("/addNewRecipe")
    @Operation(summary = "Submit a new recipe")
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400")
    })
    public ResponseEntity<PendingRecipeChefDTO> saveRecipe (@Valid @RequestBody CreateRecipeDTO dto){

        PendingRecipeChefDTO recipe = chefService.createRecipe(dto);
        return ResponseEntity.ok(recipe);
    }

    /**
     * Get Method to show the chef's pending recipes
     * @param page - the page
     * @see ChefService#showPendingRecipes(int) 
     * @return Response entity ok message 
     */
    @GetMapping("/showWaiting/{page}")
    @Operation(summary = "Show chef's pending recipes")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SliceRecipeDTO<PendingRecipeChefDTO>> showPendingRecipes (@PathVariable int page){
        SliceRecipeDTO<PendingRecipeChefDTO> recipeList = chefService.showPendingRecipes(page);
        return ResponseEntity.ok(recipeList);
    }


    /**
     * Remove a recipe that is waiting to be confirmed
     * @param recipeId - recipe ID
     * @see ChefService#removeRecipe(String)
     * @return ResponseEntity ok message
     */
    @DeleteMapping("/removeWaiting/{id}")
    @Operation(summary = "Remove a pending recipe")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<String> removeRecipe (@PathVariable("id") String recipeId){

        chefService.removeRecipe(recipeId);
        return ResponseEntity.ok("Recipe successfully removed");
    }


    /**
     * Method to delete a recipe that already exists
     * @param recipeId - recipe ID
     * @see ChefService#deleteRecipe(String)
     * @return ResponseEntity with message
     */
    @DeleteMapping("/deleteRecipe/{id}")
    @Operation(summary = "Delete an existing recipe")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<String> deleteRecipe (@PathVariable("id") String recipeId){

        chefService.deleteRecipe(recipeId);
        return ResponseEntity.ok("Recipe successfully deleted");
    }


    /**
     * Show a recipe to a chef
     * @param page - the page
     * @see ChefService#showRecipes(int)
     * @return ResponseEntity ok message
     */
    @GetMapping("/show/{page}")
    @Operation(summary = "Show published recipes")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SliceRecipeDTO<ChefPreviewRecipeDTO>> showRecipe (@PathVariable int page){
        SliceRecipeDTO<ChefPreviewRecipeDTO> recipeList = chefService.showRecipes(page);
        return ResponseEntity.ok(recipeList);
    }

    /**
     * Show to the chef his/her popular recipes
     * @param pageNumber - page number
     * @see ChefService#showPopularRecipes(int)
     * @return ResponseEntity ok message
     */
    @GetMapping("/popular/{pageNumber}")
    @Operation(summary = "Show popular recipes")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SliceRecipeDTO<ChefPreviewRecipeDTO>> popularRecipe (@PathVariable int pageNumber){
        SliceRecipeDTO<ChefPreviewRecipeDTO> recipeList = chefService.showPopularRecipes(pageNumber);
        return ResponseEntity.ok(recipeList);
    }

    /**
     * Get method to retrieve detailed information about a specific pending recipe.
     * @param recipeId - pending recipeID
     * @see ChefService#getRecipeDetails(String)
     * @return ResponseEntity containing the pending recipe's details
     */
    @GetMapping("/details/pending/{recipeId}")
    @Operation(summary = "Get pending recipe details")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<ShowRecipeDTO> getRecipeDetails (@PathVariable String recipeId){
        ShowRecipeDTO recipe = chefService.getRecipeDetails(recipeId);
        return ResponseEntity.ok(recipe);
    }
}
