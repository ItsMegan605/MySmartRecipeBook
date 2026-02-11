package it.unipi.MySmartRecipeBook.controller;

import it.unipi.MySmartRecipeBook.dto.LoginRequestDTO;
import it.unipi.MySmartRecipeBook.dto.JwtResponseDTO;
import it.unipi.MySmartRecipeBook.dto.CreateChefDTO;
import it.unipi.MySmartRecipeBook.dto.CreateFoodieDTO;
import it.unipi.MySmartRecipeBook.service.AuthService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    //Register Chef
    @PostMapping("/register/chef")
    public ResponseEntity<?> registerChef(
            @RequestBody CreateChefDTO dto) {

        authService.registerChef(dto);
        return ResponseEntity.ok("Chef registered successfully");
    }

    //Register Foodie
    @PostMapping("/register/foodie")
    public ResponseEntity<?> registerFoodie(
            @RequestBody CreateFoodieDTO dto) {

        authService.registerFoodie(dto);
        return ResponseEntity.ok("Foodie registered successfully");
    }

    //Login
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> login(
            @RequestBody LoginRequestDTO request) {

        return ResponseEntity.ok(
                authService.authenticateUser(request)
        );
    }
}
