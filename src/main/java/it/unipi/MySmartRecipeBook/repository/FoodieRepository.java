
package it.unipi.MySmartRecipeBook.repository;

import it.unipi.MySmartRecipeBook.model.Foodie;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

    @Repository
    public interface FoodieRepository extends MongoRepository<Foodie, String> {

        Optional<Foodie> findByUsername(String username);

        //login: Cerca per username e password, decidere se vogliamo email o psw
        Optional<Foodie> findByUsernameAndPassword(String username, String password);

        // Check esistenza
        boolean existsByUsername(String username);
        //boolean existsByEmail(String email);
    }