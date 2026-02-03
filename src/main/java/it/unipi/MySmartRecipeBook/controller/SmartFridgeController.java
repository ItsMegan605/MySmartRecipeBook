package it.unipi.MySmartRecipeBook.controller;

import it.unipi.MySmartRecipeBook.service.SmartFridgeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fridge") // Base URL: /api/fridge
public class SmartFridgeController {

    private final SmartFridgeService fridgeService;

    public SmartFridgeController(SmartFridgeService fridgeService) {
        this.fridgeService = fridgeService;
    }

    // --------------------------------------------------------------------------------
    // AZIONE 1: Aggiungere un ingrediente
    // URL Esempio: POST /api/fridge/user123/add
    // Corpo richiesta (Raw Text/JSON): "Pomodoro"
    // --------------------------------------------------------------------------------
    @PostMapping("/{userId}/add")
    public ResponseEntity<Void> addIngredient(
            @PathVariable String userId,       // 1. Estrae "user123" dall'URL e lo mette nella variabile userId
            @RequestBody String ingredient     // 2. Estrae "Pomodoro" dal corpo della richiesta
    ) {
        // Chiamiamo il service per salvare su Redis
        fridgeService.addIngredientToFridge(userId, ingredient);

        // Rispondiamo con 200 OK ma senza contenuto (Void), perché abbiamo solo salvato.
        return ResponseEntity.ok().build();
    }


}