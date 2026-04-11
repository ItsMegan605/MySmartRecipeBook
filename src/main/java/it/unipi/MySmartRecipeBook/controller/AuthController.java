package it.unipi.MySmartRecipeBook.controller;

import it.unipi.MySmartRecipeBook.dto.LoginRequestDTO;
import it.unipi.MySmartRecipeBook.dto.JwtResponseDTO;
import it.unipi.MySmartRecipeBook.dto.users.RegistedUserDTO;
import it.unipi.MySmartRecipeBook.service.AuthService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Period;

/**
 * Authentication controller
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint for chef registration request
     * @param dto
     * @see AuthService#registerChef(RegistedUserDTO)
     * @return ResponseEntity with message
     */

    @PostMapping("/register/chef")
    public ResponseEntity<String> registerChef (@Valid @RequestBody RegistedUserDTO dto){

        if(Period.between(dto.getBirthdate(), LocalDate.now()).getYears() < 15){
            throw new IllegalArgumentException("You must be at least 15 years old to use this service");        }

        authService.registerChef(dto);
        return ResponseEntity.ok("Registration request completed successfully. Waiting for admin approval.");
    }

    /**
     * Endpoint for the foodie's registration phase
     * @param dto with user's parameters
     * @see AuthService#registerFoodie(RegistedUserDTO)
     * @return ResponseEntity with message
     */

    @PostMapping("/register/foodie")
    public ResponseEntity<String> registerFoodie (@Valid @RequestBody RegistedUserDTO dto){

        if(Period.between(dto.getBirthdate(), LocalDate.now()).getYears() < 15){
            throw new IllegalArgumentException("You must be at least 15 years old to use this service");        }

        authService.registerFoodie(dto);
        return ResponseEntity.ok("Foodie registered successfully");
    }

    /**
     * Login endpoint for both chef and foodie
     * @param request
     * @return ResponseEntity ok message
     */

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> login (@Valid @RequestBody LoginRequestDTO request){

        return ResponseEntity.ok(authService.authenticateUser(request));
    }
}
