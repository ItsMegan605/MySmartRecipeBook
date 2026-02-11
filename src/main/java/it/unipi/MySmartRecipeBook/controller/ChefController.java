package it.unipi.MySmartRecipeBook.controller;

import it.unipi.MySmartRecipeBook.dto.ChefResponseDTO;
import it.unipi.MySmartRecipeBook.dto.UpdateChefDTO;
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

    /* =========================
       GET PROFILE
       ========================= */

    @GetMapping("/me")
    public ResponseEntity<ChefResponseDTO> getMe() {

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return ResponseEntity.ok(
                chefService.getByUsername(username)
        );
    }

    /* =========================
       UPDATE PROFILE
       ========================= */

    @PatchMapping("/me")
    public ResponseEntity<ChefResponseDTO> updateMe(
            @Valid @RequestBody UpdateChefDTO dto) {

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return ResponseEntity.ok(
                chefService.updateChef(username, dto)
        );
    }

    /* =========================
       DELETE PROFILE
       ========================= */

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe() {

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        chefService.deleteChef(username);

        return ResponseEntity.noContent().build();
    }
}
