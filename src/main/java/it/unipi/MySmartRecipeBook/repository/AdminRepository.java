package it.unipi.MySmartRecipeBook.repository;

import it.unipi.MySmartRecipeBook.model.Admin;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends MongoRepository<Admin, String> {
    Admin findByUsername(String username);

    @Query("{ '_id': ?0 }")
    @Update("{ '$pull': { 'recipesToApprove': { 'id': ?1 } } }")
    void removeRecipeFromApprovals(String recipeId);
}
