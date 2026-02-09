
package it.unipi.MySmartRecipeBook.repository;

import it.unipi.MySmartRecipeBook.model.Foodie;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FoodieRepository extends MongoRepository<Foodie, String> {
    Optional<Foodie> findByUsername(String username);
    Optional<Foodie> findById(String id);
    boolean existsFoodieById(String id);
}