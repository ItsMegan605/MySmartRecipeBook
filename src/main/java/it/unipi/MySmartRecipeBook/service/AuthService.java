package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.LoginRequestDTO;
import it.unipi.MySmartRecipeBook.dto.JwtResponseDTO;
import it.unipi.MySmartRecipeBook.dto.users.RegisteredUserDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Admin;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Foodie;
import it.unipi.MySmartRecipeBook.model.Mongo.users.PendingChef;
import it.unipi.MySmartRecipeBook.repository.Mongo.AdminRepository;
import it.unipi.MySmartRecipeBook.repository.Mongo.ChefRepository;
import it.unipi.MySmartRecipeBook.repository.Mongo.FoodieRepository;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import it.unipi.MySmartRecipeBook.security.jwt.JwtUtils;

import it.unipi.MySmartRecipeBook.utils.conversionFunctions.ChefUtilityFunctions;
import it.unipi.MySmartRecipeBook.utils.conversionFunctions.FoodieUtilityFunctions;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Authentication Service
 */
@Service
public class AuthService {

    private final ChefRepository chefRepository;
    private final FoodieRepository foodieRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final AdminRepository adminRepository;
    private final ChefUtilityFunctions chefConversions;
    private final FoodieUtilityFunctions foodieUtils;

    public AuthService(ChefRepository chefRepository, FoodieRepository foodieRepository,
                       AuthenticationManager authenticationManager, JwtUtils jwtUtils,
                       AdminRepository adminRepository, ChefUtilityFunctions chefConversions,
                       FoodieUtilityFunctions foodieUtils) {

        this.chefRepository = chefRepository;
        this.foodieRepository = foodieRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.adminRepository = adminRepository;
        this.chefConversions = chefConversions;
        this.foodieUtils = foodieUtils;
    }


    /**
     * Registers a new chef, saving it in the chef collection with a "PENDING" status
     * and adding it to the admin's list of chefs waiting for approval.
     * @param chefDTO a {@link RegisteredUserDTO} containing all the registration information of the chef
     * @throws DataIntegrityViolationException if the username is already taken by another chef or foodie
     */
    @Transactional
    public void registerChef(RegisteredUserDTO chefDTO) {

        if (chefRepository.existsByUsername(chefDTO.getUsername())
                || foodieRepository.existsByUsername(chefDTO.getUsername())) {
            throw new DataIntegrityViolationException("Username already taken");
        }

        Chef chef = chefConversions.createChefEntity(chefDTO);
        chef = chefRepository.save(chef);

        Admin admin = adminRepository.findByUsername("admin");
        PendingChef pendingChef = chefConversions.createPendingChef(chef);
        adminRepository.addChefToApprovals(admin.getId(), pendingChef);
    }


    /**
     * Registers a new foodie, saving it in the foodie collection.
     * @param foodieDTO a {@link RegisteredUserDTO} containing all the registration information of the foodie
     * @throws DataIntegrityViolationException if the username is already taken by another foodie or chef
     */
    public void registerFoodie(RegisteredUserDTO foodieDTO) {

        if (chefRepository.existsByUsername(foodieDTO.getUsername())
                || foodieRepository.existsByUsername(foodieDTO.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }

        Foodie foodie = foodieUtils.createFoodieEntity(foodieDTO);
        foodieRepository.save(foodie);
    }


    /**
     * Authenticates a registered user using the provided username and password.
     * @param request a {@link LoginRequestDTO} containing the credentials specified by the user (username and password)
     * @return a {@link JwtResponseDTO} containing the generated JWT token and the user's authorization details
     */
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
