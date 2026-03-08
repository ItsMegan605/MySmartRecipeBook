package it.unipi.MySmartRecipeBook.repository.Mongo;

import it.unipi.MySmartRecipeBook.model.Mongo.users.Admin;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.PendingRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.users.PendingChef;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends MongoRepository<Admin, String> {
    Admin findByUsername(String username);

    @Query("{ '_id': ?0 }")
    @Update("{ '$push': { 'recipes_to_approve': ?1 } }")
    void addRecipeToApprovals(String adminId, PendingRecipe recipe);

    @Query("{ '_id': ?0 }")
    @Update("{ '$pull': { 'recipes_to_approve': { 'id': ?1 } } }")
    void removeRecipeFromApprovals(String adminId, String recipeId);

    @Query("{ '_id': ?0 }")
    @Update("{ '$pull': { 'chefs_to_approve': { 'username': ?1 } } }")
    void removeChefFromApprovals(String adminId, String chefUsername);


    @Query("{ '_id': ?0 }")
    @Update("{ '$push': { 'chefs_to_approve': ?1 } }")
    void addChefToApprovals(String adminId, PendingChef chef);
}
