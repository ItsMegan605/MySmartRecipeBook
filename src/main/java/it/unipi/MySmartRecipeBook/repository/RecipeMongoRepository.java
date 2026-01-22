package it.unipi.MySmartRecipeBook.repository;

import it.unipi.MySmartRecipeBook.model.RecipeMongo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecipeMongoRepository extends MongoRepository<RecipeMongo, String> {

    // MAGIA 1: Spring legge il nome del metodo e crea la query da solo
    List<RecipeMongo> findByCategory(String category);

    // MAGIA 2: Cerca nel titolo ignorando maiuscole/minuscole
    List<RecipeMongo> findByTitleContainingIgnoreCase(String title);
}