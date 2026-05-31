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
import java.util.Optional;

/**
 * Repository for managing RecipeMongo documents in MongoDB.
 */
@Repository
public interface RecipeMongoRepository extends MongoRepository<RecipeMongo, String> {

    /**
     * Recipe status
     * @param id - recipe id
     * @return recipe details if approved
     */
    @Query("{ '_id': ?0, 'status': 'APPROVED' }")
    Optional<RecipeMongo> findApprovedById(String id);

    /**
     * Finds recipes by title (case insensitive) with pagination.
     * @param title substring that is part of the title
     * @param pageable pagination information
     * @return slice of recipes
     */
    @Query("{ 'status': ?1, 'title': { '$regex': ?0, '$options': 'i' } }")
    Slice<RecipeMongo> findRecipesByTitle(String title, String status, Pageable pageable);


    /**
     * Finds recipes by category with pagination.
     * @param category recipe category
     * @param pageable pagination information
     * @return slice of recipes
     */
    @Query("{ 'status': ?1, 'category': ?1 }")
    Slice<RecipeMongo> findByCategory(String category, String status, Pageable pageable);

    /**
     * Finds all recipes of a chef ordered by creation date (descending).
     * @param chefId chef ID
     * @return list of recipes
     */
    List<RecipeMongo> findByChef_IdOrderByCreationDateDesc(String chefId);

    /**
     * Finds recipes by a list of IDs.
     * @param ids list of recipe IDs
     * @return list of recipes
     */
    List<RecipeMongo> findByIdIn(List<String> ids);

    /**
     * Updates the save counter of a recipe.
     * @param recipeId recipe ID
     * @param i increment or decrement value
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
     * Deletes a recipe by ID.
     * @param id recipe ID
     * @return number of deleted documents
     */
    RecipeMongo deleteRecipeById(String id);

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

}