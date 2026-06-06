package it.unipi.MySmartRecipeBook.repository.Neo4j;

import it.unipi.MySmartRecipeBook.dto.recipe.RecipeSuggestionDTO;
import it.unipi.MySmartRecipeBook.model.Neo4j.RecipeNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing Recipe nodes in Neo4j.
 */

@Repository
public interface RecipeNeo4jRepository extends Neo4jRepository<RecipeNeo4j, Long> {

    /**
     * Finds recipe suggestions based on available ingredients (Smart Fridge use case).
     * Query logic:
     * - match recipes that use given ingredients
     * - count how many ingredients match
     * - require at least 3 matching ingredients
     * - retrieve chef information
     * - return results ordered by match count
     *
     * @param myIngredients list of available ingredients
     * @return list of RecipeSuggestionDTO
     */
    @Query("MATCH (i:Ingredient)-[:USED_IN]->(r:Recipe) " +
            "WHERE i.name IN $myIngredients " +
            "WITH r, count(i) AS matchCount, collect(i.name) AS matchedIngredients " +
            "WHERE matchCount >= 3 " +
            "MATCH (r)-[:WRITTEN_BY]->(c:Chef) " +
            "RETURN r.mongo_id AS id, " +
            "       r.title AS title, " +
            "       r.imageURL AS imageURL, " +
            "       c.name AS chefName, " +
            "       c.surname AS chefSurname, " +
            "       c.mongo_id AS chefId, " +
            "       matchCount, " +
            "       matchedIngredients " +
            "ORDER BY matchCount DESC")
    List<RecipeSuggestionDTO> findRecipesByIngredients(List<String> myIngredients);


    /**
     * Creates a recipe node and connects it to a chef and ingredients and the relationships are in both
     * directions for efficiency purposes.
     * @param recipeId recipe ID
     * @param title recipe title
     * @param imageURL recipe image URL
     * @param category recipe category
     * @param chefId chef ID
     * @param ingredients list of ingredient names
     */
    @Query("MERGE (c:Chef {mongo_id: $chefId}) " +
            "CREATE (r:Recipe {mongo_id: $recipeId, title: $title, imageURL: $imageURL, category : $category}) " +
            "MERGE (c)<-[:WRITTEN_BY]-(r) " +
            "MERGE (c)-[:WROTE]->(r) " +
            "WITH r " +
            "UNWIND $ingredients AS ingName " +
            "MATCH (i:Ingredient {name: ingName}) " +
            //"MATCH (i:Ingredient) WHERE toLower(trim(i.name)) = toLower(trim(ingName)) " +
            "MERGE (r)<-[:USED_IN]-(i)")
    void createRecipe(String recipeId, String title, String imageURL, String category, String chefId, List<String> ingredients);

    /**
     * Deletes a recipe node by its Mongo ID.
     * @param recipeId recipe ID
     */
    @Query("MATCH (r:Recipe {mongo_id: $recipeId}) DETACH DELETE r")
    void deleteRecipeById(String recipeId);


    /**
     * Finds similar recipes based on the number of shared ingredients.
     * Query logic:
     * - match the target recipe by its Mongo ID
     * - traverse to its ingredients and then to other recipes using those ingredients
     * - exclude the target recipe itself from the results
     * - count the shared ingredients and collect their names
     * - traverse to the Chef node to retrieve the author's details
     * - return the top 3 similar recipes ordered by the highest number of shared ingredients
     *
     * @param recipeId the Mongo ID of the target recipe
     * @return list of RecipeSuggestionDTO containing the similar recipes and their chefs
     */
    @Query("MATCH (target:Recipe {mongo_id: $recipeId})<-[:USED_IN]-(i:Ingredient)-[:USED_IN]->(other:Recipe) " +
            "WHERE target <> other " +
            "WITH other, count(i) AS sharedIngredientsCount, collect(i.name) AS sharedIngredients " +
            "MATCH (other)-[:WRITTEN_BY]->(c:Chef) " +
            "RETURN other.mongo_id AS id, " +
            "       other.title AS title, " +
            "       other.imageURL AS imageURL, " +
            "       c.name AS chefName, " +
            "       c.surname AS chefSurname, " +
            "       c.mongo_id AS chefId, " +
            "       sharedIngredientsCount AS matchCount, " +
            "       sharedIngredients AS matchedIngredients " +
            "ORDER BY matchCount DESC, other.title ASC " +
            "LIMIT 3")
    List<RecipeSuggestionDTO> findSimilarRecipes(String recipeId);

}