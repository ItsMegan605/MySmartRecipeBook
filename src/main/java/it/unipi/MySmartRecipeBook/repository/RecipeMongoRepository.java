package it.unipi.MySmartRecipeBook.repository;

import it.unipi.MySmartRecipeBook.model.Mongo.RecipeMongo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import javax.swing.*;


@Repository
public interface RecipeMongoRepository extends MongoRepository<RecipeMongo, String> {

    Slice<RecipeMongo> findByTitleContainingIgnoreCase(String titleFragment, Pageable pageable);

    Slice<RecipeMongo> findByChefName(String chefName, Pageable pageable);

    Slice<RecipeMongo> findByCategory(String category, Pageable pageable);

    Slice<RecipeMongo> findByChefId(String chefId, Pageable pageable);

    @Query("{ '_id' : ?0 }")
    @Update("{ '$inc' : { 'numSaves' : ?1 } }")
    void updateSavesCounter(String recipeId, int i);

    void deleteAllByChefId(String chefId);

    boolean existsByTitle(String title);

    Integer countByChefId(String chefId);

    Long deleteRecipeById(String id);
}