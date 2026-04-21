package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.LoginRequestDTO;
import it.unipi.MySmartRecipeBook.dto.JwtResponseDTO;
import it.unipi.MySmartRecipeBook.dto.users.RegisteredUserDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Admin;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Foodie;
import it.unipi.MySmartRecipeBook.model.Mongo.users.PendingChef;
import it.unipi.MySmartRecipeBook.repository.Mongo.AdminRepository;
import it.unipi.MySmartRecipeBook.repository.Mongo.ChefRepository;
import it.unipi.MySmartRecipeBook.repository.Mongo.FoodieRepository;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import it.unipi.MySmartRecipeBook.security.jwt.JwtUtils;

import it.unipi.MySmartRecipeBook.utils.conversionFunctions.ChefUtilityFunctions;
import it.unipi.MySmartRecipeBook.utils.conversionFunctions.FoodieUtilityFunctions;
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


    /**
     * Registration of a new chef
     * @param chefDTO - the chef's DTO
     */

    public void registerChef(RegisteredUserDTO chefDTO) {

        if (chefRepository.existsByUsername(chefDTO.getUsername())
                || foodieRepository.existsByUsername(chefDTO.getUsername())) {
            throw new DataIntegrityViolationException("Username already taken");
        }

        PendingChef chef = chefUtils.createChefEntity(chefDTO);

        Admin admin = adminRepository.findByUsername("admin");


        if(admin.getChefsToApprove()!=null) {
            for (PendingChef targetChef : admin.getChefsToApprove()) {
                if (chefUtils.chefAlreadyInserted(targetChef, chef)) {
                    throw new IllegalArgumentException("Request already sent or username already present");
                }
            }
        }

        adminRepository.addChefToApprovals(admin.getId(), chef);
    }


    /**
     * Registration of a new foodie
     * @param foodieDTO - foodie's DTO
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
     * Login function
     * @param request - the login request
     * @return the token and authorization for the user
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
