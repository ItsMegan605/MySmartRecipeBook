package it.unipi.MySmartRecipeBook.controller;

import it.unipi.MySmartRecipeBook.dto.ChefInfoDTO;
import it.unipi.MySmartRecipeBook.dto.UpdateChefDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.ChefPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.CreateRecipeDTO;
import it.unipi.MySmartRecipeBook.service.ChefService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chefs")
@PreAuthorize("hasRole('CHEF')")
public class ChefController {

    private final ChefService chefService;

    public ChefController(ChefService chefService) {
        this.chefService = chefService;
    }


    /*--------------- Retrieve chef's informations ----------------*/

    @GetMapping("/info")
    public ResponseEntity<ChefInfoDTO> getInformations() {

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return ResponseEntity.ok(chefService.getByUsername(username));
    }


    /*--------------- Change chef's informations ----------------*/

    @PostMapping("/changeInfo")
    public ResponseEntity<ChefInfoDTO> updateInformation (@Valid @RequestBody UpdateChefDTO dto){

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return ResponseEntity.ok(chefService.updateChef(username, dto));
    }


    /*----------------- Delete chef's profile ------------------*/

    @DeleteMapping("/deleteProfile")
    public ResponseEntity<Void> deleteProfile() {

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        chefService.deleteChef(username);
        return ResponseEntity.noContent().build();
    }


    /*------------------- Add new recipe --------------------*/

    @PostMapping("/addNewRecipe")
    public ResponseEntity<ChefPreviewRecipeDTO> saveRecipe (@Valid @RequestBody CreateRecipeDTO dto){

        ChefPreviewRecipeDTO recipe = chefService.createRecipe(dto);
        return ResponseEntity.ok(recipe);
    }
}
