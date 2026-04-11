package it.unipi.MySmartRecipeBook.repository.Mongo;

import it.unipi.MySmartRecipeBook.model.Mongo.recipes.RecipeMongo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import it.unipi.MySmartRecipeBook.dto.TrendAnalyticsDTO;
import org.springframework.data.mongodb.repository.Aggregation;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for managing RecipeMongo documents in MongoDB.
 */
@Repository
public interface RecipeMongoRepository extends MongoRepository<RecipeMongo, String> {

    /**
     * Finds recipes by title (case insensitive) with pagination.
     * @param titleFragment part of the title
     * @param pageable pagination information
     * @return slice of recipes
     */
    Slice<RecipeMongo> findByTitleContainingIgnoreCase(String titleFragment, Pageable pageable);

    /**
     * Finds recipes by category with pagination.
     * @param category recipe category
     * @param pageable pagination information
     * @return slice of recipes
     */
    Slice<RecipeMongo> findByCategory(String category, Pageable pageable);

    /**
     * Finds all recipes of a chef ordered by creation date (descending).
     * @param chefId chef ID
     * @return list of recipes
     */
    List<RecipeMongo> findByChef_IdOrderByCreationDateDesc(String chefId);

    /**
     * Finds recipes by chef ID with pagination.
     * @param chefId chef ID
     * @param pageable pagination information
     * @return slice of recipes
     */
    Slice<RecipeMongo> findByChef_Id(String chefId, Pageable pageable);

    /**
     * Finds recipes by a list of IDs.
     * @param ids list of recipe IDs
     * @return list of recipes
     */
    List<RecipeMongo> findByIdIn(List<String> ids);

    /**
     * Updates the save counter of a recipe.
     * @param recipeId recipe ID
     * @param i increment value
     */
    @Query("{ '_id' : ?0 }")
    @Update("{ '$inc' : { 'num_saves' : ?1 } }")
    void updateSavesCounter(String recipeId, int i);

    /**
     * Deletes all recipes of a specific chef.
     * @param chefId chef ID
     */
    void deleteAllByChefId(String chefId);

    /**
     * Checks if a recipe exists by title.
     * @param title recipe title
     * @return true if exists
     */
    boolean existsByTitle(String title);

    /**
     * Counts recipes belonging to a chef.
     * @param chefId chef ID
     * @return number of recipes
     */
    @Query(value = "{ 'chef.id' : ?0 }", count = true)
    int countByChef(String chefId);

    /**
     * Deletes a recipe by ID.
     * @param id recipe ID
     * @return number of deleted documents
     */
    Long deleteRecipeById(String id);

    /**
     * Computes category trends based on recent and previous time windows:
     * - classify recipes as recent or previous
     * - group by category
     * - compute counts
     * - compute growth rate
     *
     * @param recentDate threshold for recent period
     * @param previousDate threshold for previous period
     * @return list of TrendAnalyticsDTO
     */
    @Aggregation(pipeline = {
            "{ $addFields: { " +
                    "is_recent: { $gte: [ { $toDate: '$creation_date' }, ?0 ] }, " +
                    "is_previous: { $and: [ " +
                    "{ $lt: [ { $toDate: '$creation_date' }, ?0 ] }, " +
                    "{ $gte: [ { $toDate: '$creation_date' }, ?1 ] } " +
                    "] } " +
                    "} }",
            "{ $group: { " +
                    "_id: '$category', " +
                    "recentCount: { $sum: { $cond: [ '$is_recent', 1, 0 ] } }, " +
                    "previousCount: { $sum: { $cond: [ '$is_previous', 1, 0 ] } } " +
                    "} }",
            "{ $addFields: { totalCount: { $add: [ '$recentCount', '$previousCount' ] } } }",
            //"{ $match: { totalCount: { $gte: 5 } } }",
            "{ $addFields: { " +
                    "growthRate: { $cond: [ " +
                    "{ $gt: [ '$previousCount', 0 ] }, " +
                    "{ $divide: [ { $subtract: [ '$recentCount', '$previousCount' ] }, '$previousCount' ] }, " +
                    "null " +
                    "] } " +
                    "} }",
            "{ $project: { totalCount: 0 } }"
    })
    List<TrendAnalyticsDTO> findCategoryTrend(LocalDateTime recentDate, LocalDateTime previousDate);

    /**
     * Computes total number of saves for all recipes of a chef.
     * @param chefId chef ID
     * @return total saves
     */
    @Aggregation(pipeline = {
            "{ '$match': { 'chef.id': ?0 } }",
            "{ '$group': { '_id': null, 'total': { '$sum': '$num_saves' } } }",
            "{ '$project': { '_id': 0, 'total': 1 } }"
    })
    Integer getTotalSaves(String chefId); //TODO: anche questa non viene usata
}