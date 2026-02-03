package it.unipi.MySmartRecipeBook.repository;

import it.unipi.MySmartRecipeBook.model.Mongo.RecipeMongo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface RecipeMongoRepository extends MongoRepository<RecipeMongo, String> {

    //trova tutte le ricette di una certa categoria (es. "Primi", "Dolci")
    List<RecipeMongo> findByCategory(String category);

    //trova le ricette che contengono una certa parola nel titolo (Case Insensitive)
    List<RecipeMongo> findByTitleContainingIgnoreCase(String titleFragment);

    //trove le ricette create da uno specifico chef
    List<RecipeMongo> findByChefName(String chefName);


}