package it.unipi.MySmartRecipeBook.repository;
import it.unipi.MySmartRecipeBook.dto.PopularIngredientsDTO;
import it.unipi.MySmartRecipeBook.dto.UsedIngredientsDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.RecipeSuggestionDTO;
import it.unipi.MySmartRecipeBook.model.Neo4j.ChefNeo4j;
import it.unipi.MySmartRecipeBook.model.Neo4j.RecipeNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeNeo4jRepository extends Neo4jRepository<RecipeNeo4j, Long> {
//match dello smart fridge
    @Query("MATCH (i:Ingredient)-[:USED_IN]->(r:Recipe) " +
            "WHERE i.name IN $myIngredients " +
            "WITH r, count(i) AS matchCount, collect(i.name) AS matchedIngredients " +
            "WHERE matchCount >= 3 " +
            "MATCH (r)-[:WRITTEN_BY]->(c:Chef) " +
            "RETURN r.id AS id, " +
            "       r.title AS title, " +
            "       r.imageURL AS imageURL, " +
            "       c.name AS chefName, " +
            "       c.surname AS chefSurname, " +
            "       matchCount, " +
            "       matchedIngredients " +
            "ORDER BY matchCount DESC")
    List<RecipeSuggestionDTO> findRecipesByIngredients(List<String> myIngredients);

    @Query("CREATE (i:Ingredient {id: $id, name: $name})")
    void insertIngredient(String id, String name);

    // entrambi i sensi delle relazioni così facciamo presto sia a trovare lo chef a partire dalla ricetta che
    // eliminare tutte le ricette di uno chef
    @Query("MERGE (c:Chef {id: $chefId}) " +
            "CREATE (r:Recipe {id: $recipeId, title: $title, imageURL: $imageURL}) " +
            "MERGE (c)<-[:WRITTEN_BY]-(r) " +
            "MERGE (c)-[:WROTE]->(r) " +
            "WITH r " +
            "UNWIND $ingredients AS ingName " +
            "MATCH (i:Ingredient) WHERE toLower(trim(i.name)) = toLower(trim(ingName)) " +
            "MERGE (r)<-[:USED_IN]-(i)")
    void createRecipe(String recipeId, String title, String imageURL, String chefId, List<String> ingredients);

    @Query("MATCH (r:Recipe {id: $recipeId}) DETACH DELETE r")
    void deleteRecipeById(String recipeId);

    @Query("MATCH (c:Chef {id: $chefId}) " +
            // Usiamo OPTIONAL MATCH nel caso in cui lo chef non abbia ancora scritto nessuna ricetta
            "OPTIONAL MATCH (c)-[:WROTE]->(r:Recipe) " +
            // DETACH DELETE distrugge i nodi e TUTTE le relazioni ad essi collegate
            "DETACH DELETE c, r")
    void deleteChef(String chefId);

    @Query("MERGE (c:Chef {id: $chefId}) " +
            "SET c.name = $chefName, c.surname = $chefSurname")
    void insertChef(String chefId, String chefName, String chefSurname);


    //query per richiedere 5 ingredienti meno popolari si piò aggiungere anche i 5 più popolari in caso
    @Query("MATCH (i:Ingredient)-[:USED_IN]->(r:Recipe) " +
            "WHERE NOT toLower(i.name) IN $commonIngredients " +
            "RETURN i.name AS ingredientName, count(r) AS usageCount " +
            "ORDER BY usageCount ASC " +
            "LIMIT 5")
    List<UsedIngredientsDTO> getCommonIngredients(List<String> commonIngredients);

}
