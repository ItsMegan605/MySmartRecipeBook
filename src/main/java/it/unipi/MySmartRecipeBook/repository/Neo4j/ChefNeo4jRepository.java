package it.unipi.MySmartRecipeBook.repository.Neo4j;

import it.unipi.MySmartRecipeBook.dto.users.TopChefDTO;
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
     * Retrieves the top 3 chefs for each given category.
     * Query logic: filter recipes by category, group them and count + sorting

     * @param categories list of categories
     * @return list of TopChefDTO
     */
    @Query("MATCH (r:Recipe) WHERE r.category IN $categories " +
            "WITH DISTINCT r.category AS cat " +
            "CALL { " +
            "    WITH cat " +
            "    MATCH (c:Chef)-[:WROTE]->(r2:Recipe {category: cat}) " +
            "    RETURN c.name AS name, c.surname AS surname, count(r2) AS recipeCount " +
            "    ORDER BY recipeCount DESC " +
            "    LIMIT 3 " +
            "} " +
            "RETURN name, surname, cat AS category")
    List<TopChefDTO> findTop3ChefsByCategory(@Param("categories") List<String> categories);
}

