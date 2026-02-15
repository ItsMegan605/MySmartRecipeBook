package it.unipi.MySmartRecipeBook.repository;

import it.unipi.MySmartRecipeBook.model.Admin;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends MongoRepository<Admin, String> {
    Admin findByUsername(String username);
}
