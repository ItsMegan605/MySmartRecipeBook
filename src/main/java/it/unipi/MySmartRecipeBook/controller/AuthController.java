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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    //Register Chef
    @PostMapping("/register/chef")
    public ResponseEntity<String> registerChef (@Valid @RequestBody RegistedUserDTO dto){

        if(Period.between(dto.getBirthdate(), LocalDate.now()).getYears() < 15){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You must have at least 15 to register");
        }

        authService.registerChef(dto);
        return ResponseEntity.ok("Registration request completed successfully. Waiting for admin approval.");
    }

    //Register Foodie
    @PostMapping("/register/foodie")
    public ResponseEntity<String> registerFoodie (@Valid @RequestBody RegistedUserDTO dto){

        if(Period.between(dto.getBirthdate(), LocalDate.now()).getYears() < 15){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You must be at least 15 to register");
        }

        authService.registerFoodie(dto);
        return ResponseEntity.ok("Foodie registered successfully");
    }

    //Login per entrambi
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> login (@Valid @RequestBody LoginRequestDTO request){

        return ResponseEntity.ok(authService.authenticateUser(request));
    }
}
