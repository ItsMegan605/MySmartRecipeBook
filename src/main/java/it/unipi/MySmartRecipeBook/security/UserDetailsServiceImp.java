package it.unipi.MySmartRecipeBook.security;

import it.unipi.MySmartRecipeBook.model.Chef;
import it.unipi.MySmartRecipeBook.model.Foodie;
import it.unipi.MySmartRecipeBook.repository.ChefRepository;
import it.unipi.MySmartRecipeBook.repository.FoodieRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.Optional;


@Service
public class UserDetailsServiceImp implements UserDetailsService {

    private final ChefRepository chefRepository;
    private final FoodieRepository foodieRepository;

    public UserDetailsServiceImp(ChefRepository chefRepository,
                                 FoodieRepository foodieRepository) {
        this.chefRepository = chefRepository;
        this.foodieRepository = foodieRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Optional<Chef> chefOpt = chefRepository.findByUsername(username);
        if (chefOpt.isPresent()) {
            return UserPrincipal.buildChef(chefOpt.get());
        }

        Optional<Foodie> foodieOpt = foodieRepository.findByUsername(username);
        if (foodieOpt.isPresent()) {
            return UserPrincipal.buildFoodie(foodieOpt.get());
        }

        throw new UsernameNotFoundException("User not found");
    }
}
