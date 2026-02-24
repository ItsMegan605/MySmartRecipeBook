
package it.unipi.MySmartRecipeBook.repository;

import it.unipi.MySmartRecipeBook.model.Foodie;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FoodieRepository extends MongoRepository<Foodie, String> {

    Optional<Foodie> findByUsername(String username);
    Optional<Foodie> findById(String id);

    boolean existsByUsername(String username);

    @Query("{ '$or': [ { 'new_saved.chef.chef_id': ?0 }, { 'old_saved.chef_id': ?0 } ] }")
    List<Foodie> findFoodiesWithChefRecipes(String chefId);

    @Query("{ '$or': [ { 'new_saved.chef.chef_id': ?0 }, { 'old_saved.chef_id': ?0 } ] }")
    @Update("{ '$pull': { 'new_saved': { '_id': ?1 }, 'old_saved': { '_id': ?1 } } }")
    void deleteRecipeFromFoodies(String chefId, String recipeId);
}