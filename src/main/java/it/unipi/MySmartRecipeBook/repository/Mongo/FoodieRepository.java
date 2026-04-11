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

/**
 * Repository for managing Foodie documents in MongoDB.
 */
@Repository
public interface FoodieRepository extends MongoRepository<Foodie, String> {

    /**
     * Finds a foodie by username.
     * @param username the foodie username
     * @return optional Foodie
     */
    Optional<Foodie> findByUsername(String username);

    /**
     * Finds a foodie by ID.
     * @param id foodie ID
     * @return optional Foodie
     */
    Optional<Foodie> findById(String id);

    /**
     * Checks if a foodie exists by ID.
     * @param id foodie ID
     * @return true if exists
     */
    boolean existsById(String id);

    /**
     * Checks if a foodie exists by username.
     * @param username foodie username
     * @return true if exists
     */
    boolean existsByUsername(String username);


    /**
     * Adds a recipe to the favourites list if not already present.
     * @param foodieId foodie ID
     * @param recipeId recipe ID
     * @param recipe recipe summary
     * @return number of modified documents
     */
    @Query("{ '_id': ?0, 'saved_recipes.id': { '$ne': ?1 } }")
    @Update("{ '$push': { 'saved_recipes': { '$each': [ ?2 ], '$position': 0 } } }")
    long addRecipeToFavourites(String foodieId, String recipeId, FoodieRecipeSummary recipe);

    /**
     * Removes a recipe from the favourites list.
     * @param foodieId foodie ID
     * @param recipeId recipe ID
     * @return number of modified documents
     */
    @Query("{ '_id': ?0}")
    @Update("{ '$pull': { 'saved_recipes': { 'id': ?1} }}")
    long removeRecipeFromFavourites(String foodieId, String recipeId);

    /**
     * Adds multiple recipes to favourites if not already present.
     * @param foodieId foodie ID
     * @param recipesId list of recipe IDs
     * @param recipes list of recipe summaries
     * @return number of modified documents
     */
    @Query("{ '_id': ?0, 'saved_recipes.id': { '$nin': ?1 } }")
    @Update("{ '$push': { 'saved_recipes': { '$each': ?2 , '$position': 0 } } }")
    long addRecipesToFavourites(String foodieId, List<String> recipesId, List<FoodieRecipeSummary> recipes);

    /**
     * Computes monthly statistics of registered foodies.
     * @return list of YearAnalyticsDTO
     */
    @Aggregation(pipeline = {

            //Group by month
            "{ $group: { " +
                    "        _id: { $dateToString: { format: '%Y-%m', date: '$registration_date' } }, " +
                    "        year: { $first: { $dateToString: { format: '%Y', date: '$registration_date' } } }, " +
                    "        number: { $sum: 1 } " +
                    "} }",

            //Sort (this order will be preserved in the next push)
            "{ $sort: { 'year': -1, 'number': -1 } }",

            //Group by year
            "{ $group: { " +
                    "        _id: '$year', " +
                    "        totalRegisteredFoodies: { $sum: '$number' }, " +
                    "        monthAnalyticsDTOList: { $push: { _id: '$_id', totalFoodies: '$number' } } " +
                    "} }",

            //Final sorting
            "{$sort :  {'year' :  -1}}"
    })
    List<YearAnalyticsDTO> getMonthlyFoodiesStats();

}