package it.unipi.MySmartRecipeBook.repository;

import it.unipi.MySmartRecipeBook.model.Mongo.RecipeMongo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface RecipeMongoRepository extends MongoRepository<RecipeMongo, String> {

    //trova le ricette che contengono una certa parola nel titolo (Case Insensitive)
    Slice<RecipeMongo> findByTitleContainingIgnoreCase(String titleFragment, Pageable pageable);

    //trove le ricette create da uno specifico chef
    Slice<RecipeMongo> findByChefName(String chefName, Pageable pageable);

    Slice<RecipeMongo> findByCategory(String category, Pageable pageable);
}