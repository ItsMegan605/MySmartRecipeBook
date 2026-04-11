package it.unipi.MySmartRecipeBook.repository.Mongo;

import it.unipi.MySmartRecipeBook.model.Mongo.users.Admin;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.PendingRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.users.PendingChef;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing Admin documents in MongoDB.
 */
@Repository
public interface AdminRepository extends MongoRepository<Admin, String> {

    /**
     * Finds an admin by username.
     * @param username the admin username
     * @return the Admin entity
     */
    Admin findByUsername(String username);

    /**
     * Adds a pending recipe to the admin approval list.
     * @param adminId the ID of the admin
     * @param recipe the pending recipe to add
     */
    @Query("{ '_id': ?0 }")
    @Update("{ '$push': { 'recipes_to_approve': ?1 } }")
    void addRecipeToApprovals(String adminId, PendingRecipe recipe);

    /**
     * Removes a recipe from the approval list by its ID.
     * @param adminId the ID of the admin
     * @param recipeId the ID of the recipe to remove
     * @return number of modified documents
     */
    @Query("{ '_id': ?0 }")
    @Update("{ '$pull': { 'recipes_to_approve': { 'id': ?1 } } }")
    Integer removeRecipeFromApprovals(String adminId, String recipeId);

    /**
     * Removes a chef from the approval list by username.
     * @param adminId the ID of the admin
     * @param chefUsername the username of the chef to remove
     */
    @Query("{ '_id': ?0 }")
    @Update("{ '$pull': { 'chefs_to_approve': { 'username': ?1 } } }")
    void removeChefFromApprovals(String adminId, String chefUsername);

    /**
     * Adds a pending chef to the admin approval list.
     * @param adminId the ID of the admin
     * @param chef the pending chef to add
     */
    @Query("{ '_id': ?0 }")
    @Update("{ '$push': { 'chefs_to_approve': ?1 } }")
    void addChefToApprovals(String adminId, PendingChef chef);
}