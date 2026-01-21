
package it.unipi.MySmartRecipeBook.repository;

import it.unipi.MySmartRecipeBook.model.Foodie;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

    @Repository
    public interface FoodieRepository extends MongoRepository<Foodie, String> {
        // Login: Cerca per username e password
        Optional<Foodie> findByUsernameAndPassword(String username, String password);

        // Check esistenza
        boolean existsByUsername(String username);
    }