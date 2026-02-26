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

    boolean existsById(String id);
    boolean existsByUsername(String username);

    @Query("{ '_id' : ?0 }")
    @Update("{ '$inc' : { 'tot_saves' : ?1 } }")
    void updateTotalSaves(String chefId, int amount);

    @Query("{ '_id' : ?0, 'new_recipes.id' : ?1 }")
    @Update("{ '$inc' : { 'new_recipes.$.num_saves' : ?2 } }")
    void updateChefCounters(String chefId, String recipeId, int increment);

    @Query("{ '_id': ?0 }")
    @Update("{ " +
            "  '$pull': { 'recipes_to_confirm': { 'id': ?1 } }, " +
            "  '$inc': { 'tot_recipes': 1 }, " +
            "  '$push': { 'new_recipes': { '$each': [ ?2 ], '$position': 0, '$slice': 5 } } " +
            "}")
    void approveRecipe(String chefId, String recipeToConfirmId, ChefRecipe newRecipe);


    @Query("{ '_id': ?0 }")
    @Update("{ '$pull': { 'recipes_to_confirm': { 'id': ?1 } } }")
    Integer removeRecipeFromWaiting(String chefId, String recipeId);

    @Query("{ '_id': ?0 }")
    @Update("{ '$push': { 'recipes_to_confirm': ?1 } }")
    void addRecipeToWaiting(String chefId, ChefRecipe recipe);

}

