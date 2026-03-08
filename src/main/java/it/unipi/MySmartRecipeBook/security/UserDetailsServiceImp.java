package it.unipi.MySmartRecipeBook.security;

import it.unipi.MySmartRecipeBook.model.Mongo.users.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Foodie;
import it.unipi.MySmartRecipeBook.repository.Mongo.ChefRepository;
import it.unipi.MySmartRecipeBook.repository.Mongo.FoodieRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.Optional;

/* implementa l'interfaccia UserDetailsService di Spring Security, ha il compito è recuperare dal database le
info dell'utente quando viene effettuato un login
 */
@Service
public class UserDetailsServiceImp implements UserDetailsService {

    //Repository utilizzato per accedere alla collezione degli chef e dei foodie
    private final ChefRepository chefRepository;
    private final FoodieRepository foodieRepository;

    //costruttore
    public UserDetailsServiceImp(ChefRepository chefRepository,
                                 FoodieRepository foodieRepository) {
        this.chefRepository = chefRepository;
        this.foodieRepository = foodieRepository;
    }

    /* chiamato automaticamente al login, Spring Security utilizza il metodo loadUserByUsername per:
 * - cercare l'utente nel database
 * - costruire un oggetto UserDetails (nel nostro caso UserPrincipal)
 * - verificare successivamente la password
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        /* L'admin non è gestito come una normale entità con ruolo nel database,
         * quindi viene riconosciuto direttamente tramite username.
         */
        if(username.equals("admin")) {
            Chef admin = chefRepository.findByUsername("admin")
                    .orElseThrow(() -> new UsernameNotFoundException("Admin not found"));
            return UserPrincipal.buildAdmin(admin);
        }

        //controllo se lo username appartiene a uno chef
        Optional<Chef> chefOpt = chefRepository.findByUsername(username);
        if (chefOpt.isPresent()) {
            return UserPrincipal.buildChef(chefOpt.get());
        }

        //controllo se lo username appartiene a uno foodie
        Optional<Foodie> foodieOpt = foodieRepository.findByUsername(username);
        if (foodieOpt.isPresent()) {
            return UserPrincipal.buildFoodie(foodieOpt.get());
        }

        //eccezione se username non viene trovato nel DB
        throw new UsernameNotFoundException("User not found");
    }
}
