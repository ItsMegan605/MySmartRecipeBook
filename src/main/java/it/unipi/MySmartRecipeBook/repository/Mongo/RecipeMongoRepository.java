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


@Repository
public interface RecipeMongoRepository extends MongoRepository<RecipeMongo, String> {

    Slice<RecipeMongo> findByTitleContainingIgnoreCase(String titleFragment, Pageable pageable);

    Slice<RecipeMongo>findByCategory(String category, Pageable pageable);

    List<RecipeMongo> findByChef_IdOrderByCreationDateDesc(String chefId);
    Slice<RecipeMongo>findByChef_Id(String chefId, Pageable pageable);

    List<RecipeMongo> findByIdIn(List<String> ids);

    @Query("{ '_id' : ?0 }")
    @Update("{ '$inc' : { 'num_saves' : ?1 } }")
    void updateSavesCounter(String recipeId, int i);

    void deleteAllByChefId(String chefId);

    boolean existsByTitle(String title);

    @Query(value = "{ 'chef.id' : ?0 }", count = true)
    int countByChef(String chefId);

    Long deleteRecipeById(String id);

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


    @Aggregation(pipeline = {

            "{ '$match': { 'chef.id': ?0 } }",
            "{ '$group': { '_id': null, 'total': { '$sum': '$num_saves' } } }",
            "{ '$project': { '_id': 0, 'total': 1 } }"
    })
    Integer getTotalSaves(String chefId);
}