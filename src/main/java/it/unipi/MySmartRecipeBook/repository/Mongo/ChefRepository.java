package it.unipi.MySmartRecipeBook.repository.Mongo;


import it.unipi.MySmartRecipeBook.model.Mongo.users.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.PendingRecipe;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;
import it.unipi.MySmartRecipeBook.dto.ChefRankAnalyticsDTO;
import org.springframework.data.mongodb.repository.Aggregation;

import java.util.List;
import java.util.Optional;
/**
 * Repository for managing Chef documents in MongoDB.
 */

@Repository
public interface ChefRepository extends MongoRepository<Chef, String> {

    /**
     * Finds a chef by username.
     * @param username the chef username
     * @return optional Chef
     */
    Optional<Chef> findByUsername(String username);

    /**
     * Checks if a chef exists by ID.
     * @param id chef ID
     * @return true if exists
     */
    boolean existsById(String id);

    /**
     * Checks if a chef exists by username.
     * @param username chef username
     * @return true if exists
     */
    boolean existsByUsername(String username);


    /**
     * Removes a recipe from the pending list.
     * @param chefId chef ID
     * @param recipeId recipe ID
     * @return number of modified documents
     */
    @Query("{ '_id': ?0 }")
    @Update("{ '$pull': { 'recipes_to_confirm': { 'id': ?1 } } }")
    Integer removeRecipeFromWaiting(String chefId, String recipeId);

    /**
     * Adds a recipe to the pending list.
     * @param chefId chef ID
     * @param recipe pending recipe
     */
    @Query("{ '_id': ?0 }")
    @Update("{ '$push': { 'recipes_to_confirm': ?1 } }")
    void addRecipeToWaiting(String chefId, PendingRecipe recipe);


    /**
     * Computes a Bayesian ranking of chefs based on saves and number of recipes.
     * Thia function excludes the admin, replaces the new values with 0, computes the global average C, then
     * computes individual average R, we give the Bayesian score and sort the results.
     * @return list of ranked chefs
     */
    @Aggregation(pipeline = {

            "{ $match: { username: { $ne: 'admin' } } }",

            "{ $addFields: { " +
                    "   tot_saves: { $ifNull: ['$tot_saves', 0] }, " +
                    "   tot_recipes: { $ifNull: ['$tot_recipes', 0] } " +
                    "} }",

            "{ $setWindowFields: { partitionBy: null, output: { " +
                    "totalSavesGlobal: { $sum: '$tot_saves' }, " +
                    "totalRecipesGlobal: { $sum: '$tot_recipes' } } } }",

            "{ $addFields: { C: { $cond: [ { $eq: ['$totalRecipesGlobal', 0] }, 0, " +
                    "{ $divide: ['$totalSavesGlobal', '$totalRecipesGlobal'] } ] } } }",

            "{ $addFields: { R: { $cond: [ { $eq: ['$tot_recipes', 0] }, 0, " +
                    "{ $divide: ['$tot_saves', '$tot_recipes'] } ] } } }",

            "{ $addFields: { score: { $add: [ " +
                    "{ $multiply: [ { $divide: ['$tot_recipes', { $add: ['$tot_recipes', 5] }] }, '$R' ] }, " +
                    "{ $multiply: [ { $divide: [5, { $add: ['$tot_recipes', 5] }] }, '$C' ] } " +
                    "] } } }",

            "{ $sort: { score: -1 } }",

            "{ $setWindowFields: { sortBy: { score: -1 }, output: { rank: { $rank: {} } } } }",

            "{ $project: { rank: 1, username: 1, score: 1 } }"
    })
    List<ChefRankAnalyticsDTO> chefBayesianRanking();
}