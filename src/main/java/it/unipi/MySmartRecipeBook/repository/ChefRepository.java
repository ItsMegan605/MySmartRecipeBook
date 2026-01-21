package it.unipi.MySmartRecipeBook.repository;


import it.unipi.MySmartRecipeBook.model.Chef;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ChefRepository extends MongoRepository<Chef, String> {
    Optional<Chef> findByUsernameAndPassword(String username, String password);
}

