package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.LoginRequestDTO;
import it.unipi.MySmartRecipeBook.dto.JwtResponseDTO;
import it.unipi.MySmartRecipeBook.dto.users.RegistedUserDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Admin;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Foodie;
import it.unipi.MySmartRecipeBook.model.Mongo.users.PendingChef;
import it.unipi.MySmartRecipeBook.repository.Mongo.AdminRepository;
import it.unipi.MySmartRecipeBook.repository.Mongo.ChefRepository;
import it.unipi.MySmartRecipeBook.repository.Mongo.FoodieRepository;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import it.unipi.MySmartRecipeBook.security.jwt.JwtUtils;

import it.unipi.MySmartRecipeBook.utils.convertionFunctions.ChefUtilityFunctions;
import it.unipi.MySmartRecipeBook.utils.convertionFunctions.FoodieUtilityFunctions;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final ChefRepository chefRepository;
    private final FoodieRepository foodieRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final AdminRepository adminRepository;
    private final ChefUtilityFunctions chefUtils;
    private final FoodieUtilityFunctions foodieUtils;

    public AuthService(ChefRepository chefRepository, FoodieRepository foodieRepository,
                       AuthenticationManager authenticationManager, JwtUtils jwtUtils,
                       AdminRepository adminRepository, ChefUtilityFunctions chefUtils,
                       FoodieUtilityFunctions foodieUtils) {

        this.chefRepository = chefRepository;
        this.foodieRepository = foodieRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.adminRepository = adminRepository;
        this.chefUtils = chefUtils;
        this.foodieUtils = foodieUtils;
    }


    /* ------------------- Register a new chef ----------------------- */

    public void registerChef(RegistedUserDTO chefDTO) {

        // Controllo se lo username già esiste (sia nella collezione chefs che in quella foodies)
        if (chefRepository.existsByUsername(chefDTO.getUsername())
                || foodieRepository.existsByUsername(chefDTO.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        // Viene creata l'entità chef da inserire tra quelle in attesa di approvazione dell'admin
        PendingChef chef = chefUtils.createChefEntity(chefDTO);

        Admin admin = adminRepository.findByUsername("admin");

        // Controlliamo che tra le richieste in attesa di essere approvate non ci sia un duplicato (controlliamo nome,
        // cognome e data di nascita dello chef che si vuole registrare)
        if(admin.getChefsToApprove()!=null) {
            for (PendingChef targetChef : admin.getChefsToApprove()) {
                if (chefUtils.chefAlreadyInserted(targetChef, chef)) {
                    throw new RuntimeException("Request already sent or username already present");
                }
            }
        }

        // Aggiungiamo lo chef alla lista degli chef in attesa di approvazione da parte dell'admin
        adminRepository.addChefToApprovals(admin.getId(), chef);
    }



    /* ------------------- Register a new foodie ----------------------- */

    public void registerFoodie(RegistedUserDTO foodieDTO) {

        // Controllo se lo username già esiste (sia nella collezione chefs che in quella foodies) - lo username è
        // univoco in entrambe le collezioni
        if (chefRepository.existsByUsername(foodieDTO.getUsername())
                || foodieRepository.existsByUsername(foodieDTO.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        // Viene creata l'entità foodie e viene aggiunta alla collection foodies
        Foodie foodie = foodieUtils.createFoodieEntity(foodieDTO);
        foodieRepository.save(foodie);
    }


    /* ------------------- Login ----------------------- */

    // Richede username e password (LoginRequestDTO e mi restituisce il token con le altre informazioni
    public JwtResponseDTO authenticateUser(LoginRequestDTO request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken (request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateJwtToken(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        return new JwtResponseDTO(
                jwt,
                userPrincipal.getId(),
                authentication.getName(),
                userPrincipal.getAuthorities()
        );
    }
}
