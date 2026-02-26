
package it.unipi.MySmartRecipeBook.repository;

import it.unipi.MySmartRecipeBook.model.Foodie;
import it.unipi.MySmartRecipeBook.model.Mongo.FoodieRecipeSummary;
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

    boolean existsById(String id);
    boolean existsByUsername(String username);

    @Query("{'saved_recipes.chef.id': ?0 }")
    List<Foodie> findFoodiesWithChefRecipes(String chefId);

    @Query("{ 'saved_recipes.chef.id': ?0, 'saved_recipes.id': ?1 }")
    @Update("{ '$pull': {'saved_recipes': { 'id': ?1 } } }")
    void deleteRecipeFromFoodies(String chefId, String recipeId);

    @Query("{ '_id': ?0, 'saved_recipes.id': { '$ne': ?1 } }")
    @Update("{ '$push': { 'saved_recipes': { '$each': [ ?2 ], '$position': 0 } } }")
    long addRecipeToFavourites(String foodieId, String recipeId, FoodieRecipeSummary recipe);

    @Query("{ '_id': ?0}")
    @Update("{ '$pull': { 'saved_recipes': { 'id': ?1} }}")
    long removeRecipeFromFavourites(String foodieId, String recipeId);
}