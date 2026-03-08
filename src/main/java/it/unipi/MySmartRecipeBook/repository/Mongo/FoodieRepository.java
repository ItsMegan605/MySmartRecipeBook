
package it.unipi.MySmartRecipeBook.repository.Mongo;

import it.unipi.MySmartRecipeBook.dto.YearAnalyticsDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Foodie;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.FoodieRecipeSummary;
import org.springframework.data.mongodb.repository.Aggregation;
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

    @Query("{ '_id': ?0, 'saved_recipes.id': { '$nin': ?1 } }")
    @Update("{ '$push': { 'saved_recipes': { '$each': ?2 , '$position': 0 } } }")
    long addRecipesToFavourites(String foodieId, List<String> recipesId, List<FoodieRecipeSummary> recipes);

    @Aggregation(pipeline = {
            "{ $group: { " + // raggruppo per mese
                    "        _id: { $dateToString: { format: '%Y-%m', date: '$registration_date' } }, " +
                    "        year: { $first: { $dateToString: { format: '%Y', date: '$registration_date' } } }, " +
                    "        number: { $sum: 1 } " +
                    "} }",

            "{ $sort: { 'year': -1, 'number': -1 } }", // ordino (questo ordine verrà mantenuto nel push successivo)

            "{ $group: { " + // raggruppo per anno
                    "        _id: '$year', " +
                    "        totalRegisteredFoodies: { $sum: '$number' }, " +
                    "        monthAnalyticsDTOList: { $push: { _id: '$_id', totalFoodies: '$number' } } " +
                    "} }",
            "{$sort :  {'year' :  -1}}"
    })
    List<YearAnalyticsDTO> getMonthlyFoodiesStats();

}