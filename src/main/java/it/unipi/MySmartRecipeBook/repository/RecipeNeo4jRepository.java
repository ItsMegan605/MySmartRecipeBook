package it.unipi.MySmartRecipeBook.repository;
import it.unipi.MySmartRecipeBook.dto.recipe.RecipeSuggestionDTO;
import it.unipi.MySmartRecipeBook.model.Ingredient;
import it.unipi.MySmartRecipeBook.model.Neo4j.RecipeNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeNeo4jRepository extends Neo4jRepository<RecipeNeo4j, String> {

    @Query("MATCH (i:Ingredient)-[:USED_IN]->(r:Recipe) " +
            "WHERE i.name IN $myIngredients " +
            "WITH r, count(i) AS matchCount, collect(i.name) AS matchedIngredients " +
            "WHERE matchCount >= 3 " +
            "RETURN r.id AS id, " +
            "       r.title AS title, " +
            "       r.imageURL AS imageURL, " +
            "       matchCount, " +
            "       matchedIngredients " +
            "ORDER BY matchCount DESC")

    List<RecipeSuggestionDTO> findRecipesByIngredients(List<String> myIngredients);

    @Query("CREATE (i:Ingredient {id: $id, name: $name})")
    void insertIngredient(String id, String name);

    @Query("CREATE (r:Recipe {id: $recipeId, title: $title, chefId: $chefId}) " +
            "WITH r " +
            "UNWIND $ingredients AS ingName " +
            "MATCH (i:Ingredient) WHERE toLower(trim(i.name)) = toLower(trim(ingName)) " +
            "MERGE (r)<-[:USED_IN]-(i)")
    void createRecipe(String recipeId, String title, String chefId, List<String> ingredients);

    @Query("MATCH (r:Recipe {id: $recipeId}) DETACH DELETE r")
    void deleteRecipeById(String recipeId);
}
