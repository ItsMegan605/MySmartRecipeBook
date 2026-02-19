package it.unipi.MySmartRecipeBook.repository;

import it.unipi.MySmartRecipeBook.model.Mongo.RecipeMongo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RecipeMongoRepository extends MongoRepository<RecipeMongo, String> {

    Slice<RecipeMongo> findByTitleContainingIgnoreCase(String titleFragment, Pageable pageable);

    Slice<RecipeMongo> findByChefName(String chefName, Pageable pageable);

    Slice<RecipeMongo> findByCategory(String category, Pageable pageable);

    Slice<RecipeMongo> findByChefMongoId(String chefId, Pageable pageable);
}