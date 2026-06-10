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
     *
     * @param targetMongoId
     * @return
     */
    @Query("""
        MATCH (target:Chef {mongo_id: $targetMongoId})-[:WROTE]->(r1:Recipe)-[:USES]->(i:Ingredient)
        WITH target, i, collect(DISTINCT r1.category) AS targetCategories
        WHERE COUNT { (i)-[:USED_IN]->() } < 350
        WITH DISTINCT target, i, targetCategories
        MATCH (i)-[:USED_IN]->(r2:Recipe)-[:WRITTEN_BY]->(other:Chef)
        WHERE target <> other AND r2.category IN targetCategories
        WITH other, count(DISTINCT i) AS sharedIngredientsScore
        ORDER BY sharedIngredientsScore DESC
        LIMIT 3
        RETURN other.mongo_id AS id, 
               other.name AS name, 
               other.surname AS surname
        """)
    List<ChefInfoDTO> findSimilarChefs(String targetMongoId);

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

