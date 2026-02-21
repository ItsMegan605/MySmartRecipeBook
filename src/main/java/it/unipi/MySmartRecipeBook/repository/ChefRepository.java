package it.unipi.MySmartRecipeBook.repository;


import it.unipi.MySmartRecipeBook.model.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.ChefRecipe;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ChefRepository extends MongoRepository<Chef, String> {
    Optional<Chef> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("{ '_id' : ?0 }")
    @Update("{ '$inc' : { 'totalSaves' : ?1 } }")
    void updateTotalSaves(String chefId, int amount);

    @Query("{ '_id' : ?0, 'newRecipes._id' : ?1 }")
    @Update("{ '$inc' : { 'newRecipes.$.numSaves' : ?2 } }")
    void updateChefCounters(String chefId, String recipeId, int increment);

    @Query("{ '_id': ?0 }")
    @Update("{ " +
            "  '$pull': { 'recipesToConfirm': { 'id': ?1 } }, " +
            "  '$inc': { 'totalRecipes': 1 }, " +
            "  '$push': { 'newRecipes': { '$each': [ ?2 ], '$position': 0, '$slice': 5 } } " +
            "}")
    void approveRecipe(String chefId, String recipeToConfirmId, ChefRecipe newRecipe);


    @Query("{ '_id': ?0 }")
    @Update("{ '$pull': { 'recipesToConfirm': { 'id': ?1 } } }")
    void removeRecipeFromWaiting(String chefId, String recipeId);
}

