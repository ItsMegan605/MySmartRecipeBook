package it.unipi.MySmartRecipeBook.controller;

import it.unipi.MySmartRecipeBook.dto.ChefResponseDTO;
import it.unipi.MySmartRecipeBook.dto.CreateChefDTO;
import it.unipi.MySmartRecipeBook.dto.UpdateChefDTO;
import it.unipi.MySmartRecipeBook.model.Chef;
import it.unipi.MySmartRecipeBook.service.ChefService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chefs")
public class ChefController {

    private final ChefService chefService;

    public ChefController(ChefService chefService) {
        this.chefService = chefService;
    }

    /* =========================
       CREATE CHEF
       ========================= */
    @PostMapping
    public ResponseEntity<ChefResponseDTO> createChef(
            @Valid @RequestBody CreateChefDTO dto) {

        Chef chef = chefService.createChef(dto);

        ChefResponseDTO response = new ChefResponseDTO(
                chef.getUsername(),
                chef.getName(),
                chef.getSurname(),
                chef.getEmail()
        );

        return ResponseEntity.ok(response);
    }

    /* =========================
       GET CHEF
       ========================= */
    @GetMapping("/{username}")
    public ResponseEntity<ChefResponseDTO> getChef(
            @PathVariable String username) {

        return chefService.getChefByUsername(username)
                .map(chef -> new ChefResponseDTO(
                        chef.getUsername(),
                        chef.getName(),
                        chef.getSurname(),
                        chef.getEmail()
                ))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /* =========================
       UPDATE CHEF
       ========================= */
    @PutMapping("/{username}")
    public ResponseEntity<ChefResponseDTO> updateChef(
            @PathVariable String username,
            @Valid @RequestBody UpdateChefDTO dto) {

        Chef chef = chefService.updateChef(username, dto);

        return ResponseEntity.ok(
                new ChefResponseDTO(
                        chef.getUsername(),
                        chef.getName(),
                        chef.getSurname(),
                        chef.getEmail()
                )
        );
    }

    /* =========================
       DELETE CHEF
       ========================= */
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteChef(
            @PathVariable String username) {

        chefService.deleteChef(username);
        return ResponseEntity.noContent().build();
    }
}



/*

// 1. Diciamo a Spring che questo è un componente Web che risponde con dati JSON
@RestController
@RequestMapping("/api/chef")
public class ChefController {

    private final ChefService chefService;

    public ChefController(ChefService chefService) {
        this.chefService = chefService;
    }
    //capire se mettere register o no

    public static class LoginRequest {
        public String username;
        public String password;
    }
    //capire se li vogliamo far registrare

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Chef chef = chefService.login(request.username, request.password);

        if (chef != null) {
            return ResponseEntity.ok(chef);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenziali errate");
        }
    }

    // 4. Definiamo un endpoint POST. L'URL completo sarà: POST /api/chef/recipe
    // Si usa POST perché stiamo CREANDO qualcosa di nuovo nel database.
    @PostMapping("/recipe")
    public ResponseEntity<String> createRecipe(@RequestBody RecipeDTO recipeDTO) {
        // @RequestBody: Prende il JSON inviato dall'utente e lo trasforma nell'oggetto 'recipeDTO'.

        // 5. Passiamo la patata bollente al Service (il Cuoco). Il Controller non sa nulla di Mongo o Neo4j.
        chefService.createRecipe(recipeDTO);

        // 6. Restituiamo una risposta HTTP 200 (OK) con un messaggio.
        // ResponseEntity è una "busta" che contiene lo status code e il corpo della risposta.
        return ResponseEntity.ok("Ricetta creata con successo!");
    }
}
*/
