package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.LoginRequestDTO;
import it.unipi.MySmartRecipeBook.dto.JwtResponseDTO;
import it.unipi.MySmartRecipeBook.dto.CreateChefDTO;
import it.unipi.MySmartRecipeBook.dto.foodie.UpdateStandardFoodieDTO;
import it.unipi.MySmartRecipeBook.model.Chef;
import it.unipi.MySmartRecipeBook.model.Foodie;
import it.unipi.MySmartRecipeBook.repository.ChefRepository;
import it.unipi.MySmartRecipeBook.repository.FoodieRepository;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import it.unipi.MySmartRecipeBook.security.jwt.JwtUtils;

import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class AuthService {

    private final ChefRepository chefRepository;
    private final FoodieRepository foodieRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthService(ChefRepository chefRepository,
                       FoodieRepository foodieRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils) {

        this.chefRepository = chefRepository;
        this.foodieRepository = foodieRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    // 👨‍🍳 REGISTER CHEF
    public void registerChef(CreateChefDTO dto) {

        if (chefRepository.existsByUsername(dto.getUsername())
                || foodieRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        Chef chef = new Chef();
        chef.setUsername(dto.getUsername());
        chef.setEmail(dto.getEmail());
        chef.setPassword(passwordEncoder.encode(dto.getPassword()));

        chef.setName(dto.getName());
        chef.setSurname(dto.getSurname());
        chef.setBirthdate(dto.getBirthdate());
        chef.setRegisteredDate(new Date());

        chefRepository.save(chef);
    }

    //REGISTER FOODIE
    public void registerFoodie(UpdateStandardFoodieDTO dto) {

        if (chefRepository.existsByUsername(dto.getUsername())
                || foodieRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        Foodie foodie = new Foodie();
        foodie.setUsername(dto.getUsername());
        foodie.setEmail(dto.getEmail());
        foodie.setPassword(passwordEncoder.encode(dto.getPassword()));

        foodie.setName(dto.getName());
        foodie.setSurname(dto.getSurname());
        foodie.setBirthdate(dto.getBirthdate());
        foodie.setRegistrationDate(new Date());


        foodieRepository.save(foodie);
    }

    //LOGIN
    public JwtResponseDTO authenticateUser(LoginRequestDTO request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()
                        )
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateJwtToken(authentication);

        UserPrincipal userPrincipal =
                (UserPrincipal) authentication.getPrincipal();

        return new JwtResponseDTO(
                jwt,
                userPrincipal.getUsername(),
                userPrincipal.getAuthorities()
        );
    }
}
