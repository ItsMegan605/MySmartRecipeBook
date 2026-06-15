package it.unipi.MySmartRecipeBook.repository.Neo4j;

import it.unipi.MySmartRecipeBook.dto.IngredientSuggestionDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.RecipeSuggestionDTO;
import it.unipi.MySmartRecipeBook.model.Neo4j.RecipeNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
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
     * @param chefId chef ID
     * @param ingredients list of ingredient names
     */
    @Query("MERGE (c:Chef {mongo_id: $chefId}) " +
            "CREATE (r:Recipe {mongo_id: $recipeId, title: $title, imageURL: $imageURL, category: $category}) " +
            "MERGE (c)<-[:WRITTEN_BY]-(r) " +
            "MERGE (c)-[:WROTE]->(r) " +
            "WITH r " +
            "UNWIND $ingredients AS ingName " +
            "MATCH (i:Ingredient {name: ingName}) " +
            "MERGE (r)<-[:USED_IN]-(i) " +
            "MERGE (r)-[:USES]->(i)")
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
    @Query("MATCH (target:Recipe {mongo_id: $recipeId})-[:USES]->(i:Ingredient)-[:USED_IN]->(other:Recipe) " +
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

    /**
     * Finds suggested complementary ingredients based on their co-occurrence in recipes.
     * This method executes a Neo4j Cypher query that performs the following operations:
     * - Matches target ingredients from the provided list and finds the recipes they are used in.
     * - Finds other ingredients (co-occurrences) used in those same recipes.
     * - Filters out any ingredients that are present in the ignored list.
     * - Counts how often the target and suggested ingredients appear together to determine the strength of the association.
     * - Orders the pairs by occurrence in descending order.
     * - Returns the original ingredient mapped to its top 3 most frequently paired suggested ingredients.
     *
     * @param ingredientList the list of target ingredient names to base the suggestions on
     * @param ignoredIngredients a list of ingredient names to explicitly exclude from the final suggestions
     * @return a list of {@link IngredientSuggestionDTO} containing the target ingredients and their top suggestions
     */
    @Query("MATCH (target:Ingredient)-[:USED_IN]->(r:Recipe)-[:USES]->(other:Ingredient) " +
            "WHERE target.name IN $ingredientList " +
            "AND NOT other.name IN $ignoredIngredients " +
            "WITH target.name AS originalIngredient, other.name AS suggestedIngredient, count(r) AS occurrences " +
            "ORDER BY occurrences DESC " +
            "RETURN originalIngredient, collect(suggestedIngredient)[0..3] AS suggestedIngredients")
    List<IngredientSuggestionDTO> findSuggestedIngredientsForList(
            @Param("ingredientList") List<String> ingredientList,
            @Param("ignoredIngredients") List<String> ignoredIngredients
    );

}