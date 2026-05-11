package it.unipi.MySmartRecipeBook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.MySmartRecipeBook.dto.LoginRequestDTO;
import it.unipi.MySmartRecipeBook.dto.JwtResponseDTO;
import it.unipi.MySmartRecipeBook.dto.users.RegisteredUserDTO;
import it.unipi.MySmartRecipeBook.service.AuthService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Period;

/**
 * Authentication controller
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint for chef registration request
     * @param dto
     * @see AuthService#registerChef(RegisteredUserDTO)
     * @return ResponseEntity with message
     */
    @PostMapping("/register/chef")
    @Operation(summary = "Register a new chef")
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400")
    })
    public ResponseEntity<String> registerChef (@Valid @RequestBody RegisteredUserDTO dto){

        if(Period.between(dto.getBirthdate(), LocalDate.now()).getYears() < 15){
            throw new IllegalArgumentException("You must be at least 15 years old to use this service");        }

        authService.registerChef(dto);
        return ResponseEntity.ok("Registration request completed successfully. Waiting for admin approval.");
    }

    /**
     * Endpoint for the foodie's registration phase
     * @param dto with user's parameters
     * @see AuthService#registerFoodie(RegisteredUserDTO)
     * @return ResponseEntity with message
     */
    @PostMapping("/register/foodie")
    @Operation(summary = "Register a new foodie")
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400")
    })
    public ResponseEntity<String> registerFoodie (@Valid @RequestBody RegisteredUserDTO dto){

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
    @Operation(summary = "User login")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<JwtResponseDTO> login (@Valid @RequestBody LoginRequestDTO request){

        return ResponseEntity.ok(authService.authenticateUser(request));
    }
}
