package it.unipi.MySmartRecipeBook.repository.Neo4j;

import it.unipi.MySmartRecipeBook.dto.users.ChefInfoDTO;
import it.unipi.MySmartRecipeBook.model.Neo4j.ChefNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for managing Chef nodes in Neo4j.
 */
public interface ChefNeo4jRepository extends Neo4jRepository<ChefNeo4j, String> {

    /**
     * Finds similar chefs based on the number of shared ingredients in their recipes.
     * Query logic:
     * - match the target chef by their Mongo ID
     * - traverse to their recipes and the ingredients used
     * - collect the categories of the target's recipes
     * - filter out ingredients used in 350 or more recipes to optimize execution
     * - pass distinct values forward
     * - traverse from those ingredients to other recipes and their chefs
     * - exclude the target chef and ensure recipe categories match
     * - count the shared ingredients
     * - return the top 3 similar chefs ordered by the highest shared ingredients score
     *
     * @param targetMongoId the Mongo ID of the target chef
     * @return list of ChefInfoDTO containing the similar chefs
     */
    @Query("MATCH (target:Chef {mongo_id: $targetMongoId})-[:WROTE]->(r1:Recipe)-[:USES]->(i:Ingredient) " +
            "WITH target, i, collect(DISTINCT r1.category) AS targetCategories " +
            "WHERE COUNT { (i)-[:USED_IN]->() } < 350 " +
            "WITH DISTINCT target, i, targetCategories " +
            "MATCH (i)-[:USED_IN]->(r2:Recipe)-[:WRITTEN_BY]->(other:Chef) " +
            "WHERE target <> other AND r2.category IN targetCategories " +
            "WITH other, count(DISTINCT i) AS sharedIngredientsScore " +
            "ORDER BY sharedIngredientsScore DESC " +
            "LIMIT 3 " +
            "RETURN other.mongo_id AS id, " +
            "       other.name AS name, " +
            "       other.surname AS surname")
    List<ChefInfoDTO> findSimilarChefs(@Param("targetMongoId") String targetMongoId);


    /**
     * Deletes a chef and all related recipes.
     * Uses OPTIONAL MATCH in case the chef has no recipes.
     * DETACH DELETE removes nodes and all connected relationships.
     *
     * @param chefId chef ID
     */
    @Query("MATCH (c:Chef {mongo_id: $chefId}) " +
            "OPTIONAL MATCH (c)-[:WROTE]->(r:Recipe) " +
            "DETACH DELETE c, r")
    void deleteChef(@Param("chefId") String chefId);
}

