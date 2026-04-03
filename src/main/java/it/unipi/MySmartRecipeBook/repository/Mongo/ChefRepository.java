package it.unipi.MySmartRecipeBook.repository.Mongo;

import it.unipi.MySmartRecipeBook.model.Mongo.users.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.ChefPendingRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.ChefRecipeSummary;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.OldRecipe;
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
 *
 * Provides methods for:
 * - retrieving chefs
 * - updating counters and recipe lists
 * - handling pending recipes
 * - computing analytics (ranking)
 */
@Repository
public interface ChefRepository extends MongoRepository<Chef, String> {

    /**
     * Finds a chef by username.
     *
     * @param username the chef username
     * @return optional Chef
     */
    Optional<Chef> findByUsername(String username);

    /**
     * Checks if a chef exists by ID.
     *
     * @param id chef ID
     * @return true if exists
     */
    boolean existsById(String id);

    /**
     * Checks if a chef exists by username.
     *
     * @param username chef username
     * @return true if exists
     */
    boolean existsByUsername(String username);

    /**
     * Updates total saves counter for a chef.
     *
     * @param chefId chef ID
     * @param amount increment value
     */
    @Query("{ '_id' : ?0 }")
    @Update("{ '$inc' : { 'tot_saves' : ?1 } }")
    void updateTotalSaves(String chefId, int amount);

    /**
     * Updates save counter for a specific recipe inside new_recipes.
     *
     * @param chefId chef ID
     * @param recipeId recipe ID
     * @param increment increment value
     */
    @Query("{ '_id' : ?0, 'new_recipes.id' : ?1 }")
    @Update("{ '$inc' : { 'new_recipes.$.tot_saves' : ?2 } }")
    void updateChefCounters(String chefId, String recipeId, int increment);

    /**
     * Approves a recipe:
     * - removes it from pending list
     * - increments total recipes
     * - adds it to new_recipes (limited to 5 elements)
     *
     * @param chefId chef ID
     * @param recipeToConfirmId pending recipe ID
     * @param newRecipe summary of the approved recipe
     */
    @Query("{ '_id': ?0 }")
    @Update("{ " +
            "  '$pull': { 'recipes_to_confirm': { 'id': ?1 } }, " +
            "  '$inc': { 'tot_recipes': 1 }, " +
            "  '$push': { 'new_recipes': { '$each': [ ?2 ], '$position': 0, '$slice': 5 } } " +
            "}")
    void approveRecipe(String chefId, String recipeToConfirmId, ChefRecipeSummary newRecipe);

    /**
     * Removes a recipe from the pending list.
     *
     * @param chefId chef ID
     * @param recipeId recipe ID
     * @return number of modified documents
     */
    @Query("{ '_id': ?0 }")
    @Update("{ '$pull': { 'recipes_to_confirm': { 'id': ?1 } } }")
    Integer removeRecipeFromWaiting(Object chefId, String recipeId);

    /**
     * Adds a recipe to the pending list.
     *
     * @param chefId chef ID
     * @param recipe pending recipe
     */
    @Query("{ '_id': ?0 }")
    @Update("{ '$push': { 'recipes_to_confirm': ?1 } }")
    void addRecipeToWaiting(String chefId, ChefPendingRecipe recipe);

    /**
     * Updates multiple chef fields related to saved recipes.
     *
     * @param chefId chef ID
     * @param totalRecipes total number of recipes
     * @param totSaves total saves
     * @param newRecipes list of new recipes
     * @param oldRecipes list of old recipes
     * @param popularRecipes list of popular recipes
     */
    @Query("{ '_id': ?0 }")
    @Update("{ '$set': { 'tot_recipes': ?1, 'tot_saves': ?2, 'new_recipes': ?3, 'old_recipes': ?4, 'popular_recipes': ?5 } }")
    void addChefNewSaved(String chefId, int totalRecipes, int totSaves,
                         List<ChefRecipeSummary> newRecipes,
                         List<OldRecipe> oldRecipes,
                         List<ChefRecipeSummary> popularRecipes);

    /**
     * Computes a Bayesian ranking of chefs based on saves and number of recipes.
     *
     * Pipeline steps:
     * - exclude admin
     * - replace null values with 0
     * - compute global average (C)
     * - compute individual average (R)
     * - compute Bayesian score (m = 5)
     * - sort by score
     * - assign rank
     * - return rank, username, score
     *
     * @return list of ranked chefs
     */
    @Aggregation(pipeline = {

            //exclude admin
            "{ $match: { username: { $ne: 'admin' } } }",

            // Replace null values with 0
            "{ $addFields: { " +
                    "   tot_saves: { $ifNull: ['$tot_saves', 0] }, " +
                    "   tot_recipes: { $ifNull: ['$tot_recipes', 0] } " +
                    "} }",

            //compute global average C
            "{ $setWindowFields: { partitionBy: null, output: { " +
                    "totalSavesGlobal: { $sum: '$tot_saves' }, " +
                    "totalRecipesGlobal: { $sum: '$tot_recipes' } } } }",

            "{ $addFields: { C: { $cond: [ { $eq: ['$totalRecipesGlobal', 0] }, 0, " +
                    "{ $divide: ['$totalSavesGlobal', '$totalRecipesGlobal'] } ] } } }",

            //compute individual average R
            "{ $addFields: { R: { $cond: [ { $eq: ['$tot_recipes', 0] }, 0, " +
                    "{ $divide: ['$tot_saves', '$tot_recipes'] } ] } } }",

            //bayesian score (m = 5)
            "{ $addFields: { score: { $add: [ " +
                    "{ $multiply: [ { $divide: ['$tot_recipes', { $add: ['$tot_recipes', 5] }] }, '$R' ] }, " +
                    "{ $multiply: [ { $divide: [5, { $add: ['$tot_recipes', 5] }] }, '$C' ] } " +
                    "] } } }",

            //sort by score
            "{ $sort: { score: -1 } }",

            //add rank
            "{ $setWindowFields: { sortBy: { score: -1 }, output: { rank: { $rank: {} } } } }",

            //final output
            "{ $project: { rank: 1, username: 1, score: 1 } }"
    })
    List<ChefRankAnalyticsDTO> ChefBayesianRanking();
}