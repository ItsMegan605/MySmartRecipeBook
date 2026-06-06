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
     * Submits a registration request for a new chef. The request will remain pending until approved by an admin.
     * @param registrationDTO the {@link RegisteredUserDTO} containing the chef's registration details
     * @return a {@link ResponseEntity} containing a success message confirming the pending status
     * @throws IllegalArgumentException if the applicant is under 15 years of age
     * @see AuthService#registerChef(RegisteredUserDTO)
     */
    @PostMapping("/register/chef")
    @Operation(summary = "Register a new chef")
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400")
    })
    public ResponseEntity<String> registerChef (@Valid @RequestBody RegisteredUserDTO registrationDTO){

        if(Period.between(registrationDTO.getBirthdate(), LocalDate.now()).getYears() < 15){
            throw new IllegalArgumentException("You must be at least 15 years old to use this service");        }

        authService.registerChef(registrationDTO);
        return ResponseEntity.ok("Registration request completed successfully. Waiting for admin approval.");
    }


    /**
     * Registers a new foodie in the application.
     * @param registrationDTO the {@link RegisteredUserDTO} containing the foodie's registration details
     * @return a {@link ResponseEntity} containing a success message
     * @throws IllegalArgumentException if the applicant is under 15 years of age
     * @see AuthService#registerFoodie(RegisteredUserDTO)
     */
    @PostMapping("/register/foodie")
    @Operation(summary = "Register a new foodie")
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400")
    })
    public ResponseEntity<String> registerFoodie (@Valid @RequestBody RegisteredUserDTO registrationDTO){

        if(Period.between(registrationDTO.getBirthdate(), LocalDate.now()).getYears() < 15){
            throw new IllegalArgumentException("You must be at least 15 years old to use this service");        }

        authService.registerFoodie(registrationDTO);
        return ResponseEntity.ok("Foodie registered successfully");
    }


    /**
     * Authenticates a user (either a chef or a foodie) and generates a JWT token for session management.
     * @param request the {@link LoginRequestDTO} containing the user's credentials (username and password)
     * @return a {@link ResponseEntity} containing the {@link JwtResponseDTO} with the generated authentication token
     * @see AuthService#authenticateUser(LoginRequestDTO)
     */
    @PostMapping("/login")
    @Operation(summary = "User login")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<JwtResponseDTO> login (@Valid @RequestBody LoginRequestDTO request){

        return ResponseEntity.ok(authService.authenticateUser(request));
    }
}
