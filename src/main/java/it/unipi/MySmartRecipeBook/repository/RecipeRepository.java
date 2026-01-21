package it.unipi.MySmartRecipeBook.repository;
import it.unipi.MySmartRecipeBook.model.Recipe;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

    @Repository
    public interface RecipeRepository extends MongoRepository<Recipe, String> {

        // Trova ricette per nome (contiene stringa, case insensitive)
        List<Recipe> findByTitleContainingIgnoreCase(String title);

        // Trova per categoria
        List<Recipe> findByCategory(String category);

        // Esempio query complessa: cerca ricette che contengono ALMENO uno degli ingredienti passati
        List<Recipe> findByIngredientsIn(List<String> ingredients);

        // Cerca per nome dello Chef
        List<Recipe> findByChefName(String chefName);
    }

