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

@Repository
public interface ChefRepository extends MongoRepository<Chef, String> {
    Optional<Chef> findByUsername(String username);

    boolean existsById(String id);
    boolean existsByUsername(String username);

    @Query("{ '_id' : ?0 }")
    @Update("{ '$inc' : { 'tot_saves' : ?1 } }")
    void updateTotalSaves(String chefId, int amount);

    @Query("{ '_id' : ?0, 'new_recipes.id' : ?1 }")
    @Update("{ '$inc' : { 'new_recipes.$.tot_saves' : ?2 } }")
    void updateChefCounters(String chefId, String recipeId, int increment);

    @Query("{ '_id': ?0 }")
    @Update("{ " +
            "  '$pull': { 'recipes_to_confirm': { 'id': ?1 } }, " +
            "  '$inc': { 'tot_recipes': 1 }, " +
            "  '$push': { 'new_recipes': { '$each': [ ?2 ], '$position': 0, '$slice': 5 } } " +
            "}")
    void approveRecipe(String chefId, String recipeToConfirmId, ChefRecipeSummary newRecipe);


    @Query("{ '_id': ?0 }")
    @Update("{ '$pull': { 'recipes_to_confirm': { 'id': ?1 } } }")
    Integer removeRecipeFromWaiting(Object chefId, String recipeId);

    @Query("{ '_id': ?0 }")
    @Update("{ '$push': { 'recipes_to_confirm': ?1 } }")
    void addRecipeToWaiting(String chefId, ChefPendingRecipe recipe);

    @Query("{ '_id': ?0 }")
    @Update("{ '$set': { 'tot_recipes': ?1, 'tot_saves': ?2, 'new_recipes': ?3, 'old_recipes': ?4, 'popular_recipes': ?5 } }")
    void addChefNewSaved(String chefId, int totalRecipes, int totSaves,
                         List<ChefRecipeSummary> newRecipes,
                         List<OldRecipe> oldRecipes,
                         List<ChefRecipeSummary> popularRecipes);

    @Aggregation(pipeline = {

            //Escludio admin
            "{ $match: { username: { $ne: 'admin' } } }",

            //Mettiamo 0 se null
            "{ $addFields: { " +
                    "   tot_saves: { $ifNull: ['$tot_saves', 0] }, " +
                    "   tot_recipes: { $ifNull: ['$tot_recipes', 0] } " +
                    "} }",

            //Calcolo globale C
            "{ $setWindowFields: { partitionBy: null, output: { " +
                    "totalSavesGlobal: { $sum: '$tot_saves' }, " +
                    "totalRecipesGlobal: { $sum: '$tot_recipes' } } } }",

            "{ $addFields: { C: { $cond: [ { $eq: ['$totalRecipesGlobal', 0] }, 0, " +
                    "{ $divide: ['$totalSavesGlobal', '$totalRecipesGlobal'] } ] } } }",

            //Media individuale R
            "{ $addFields: { R: { $cond: [ { $eq: ['$tot_recipes', 0] }, 0, " +
                    "{ $divide: ['$tot_saves', '$tot_recipes'] } ] } } }",

            //Bayesian score (m = 5)
            "{ $addFields: { score: { $add: [ " +
                    "{ $multiply: [ { $divide: ['$tot_recipes', { $add: ['$tot_recipes', 5] }] }, '$R' ] }, " +
                    "{ $multiply: [ { $divide: [5, { $add: ['$tot_recipes', 5] }] }, '$C' ] } " +
                    "] } } }",

            //Ordiniamo
            "{ $sort: { score: -1 } }",

            // 7Aggiungiamo rank automatico
            "{ $setWindowFields: { sortBy: { score: -1 }, output: { rank: { $rank: {} } } } }",

            //Output finale pulito
            "{ $project: { rank: 1, username: 1, score: 1 } }"
    })
    List<ChefRankAnalyticsDTO> ChefBayesianRanking();

}

