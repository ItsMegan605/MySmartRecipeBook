package it.unipi.MySmartRecipeBook.security;

import it.unipi.MySmartRecipeBook.model.Mongo.users.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Foodie;
import it.unipi.MySmartRecipeBook.repository.Mongo.ChefRepository;
import it.unipi.MySmartRecipeBook.repository.Mongo.FoodieRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.Optional;

/**
 * Implementation of Spring Security's UserDetailsService.
 * This service is responsible for retrieving user information
 * from the database during the authentication process.
 */
@Service
public class UserDetailsServiceImp implements UserDetailsService {

    private final ChefRepository chefRepository;
    private final FoodieRepository foodieRepository;


    public UserDetailsServiceImp(ChefRepository chefRepository,
                                 FoodieRepository foodieRepository) {
        this.chefRepository = chefRepository;
        this.foodieRepository = foodieRepository;
    }

    /**
     * Loads a user by username.
     * This method is automatically called during login.
     * Spring Security uses it to:
     * - retrieve the user from the database
     * - build a UserDetails object (UserPrincipal)
     * - validate the password afterward
     *
     * @param username the username provided during login
     * @return a UserDetails object representing the user
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) {

        /*
         * The admin user is not managed as a standard role in the database,
         * so it is identified directly by its username.
         */
        if(username.equals("admin")) {
            Chef admin = chefRepository.findByUsername("admin")
                    .orElseThrow(() -> new UsernameNotFoundException("Admin not found"));
            return UserPrincipal.buildAdmin(admin);
        }

        //check if the username belongs to a Chef
        Optional<Chef> chefOpt = chefRepository.findByUsername(username);
        if (chefOpt.isPresent()) {
            return UserPrincipal.buildChef(chefOpt.get());
        }

        //check if the username belongs to a Foodie
        Optional<Foodie> foodieOpt = foodieRepository.findByUsername(username);
        if (foodieOpt.isPresent()) {
            return UserPrincipal.buildFoodie(foodieOpt.get());
        }

        //throw exception if user is not found in the database
        throw new UsernameNotFoundException("User not found");
    }
}