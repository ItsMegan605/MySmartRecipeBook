package it.unipi.MySmartRecipeBook.repository;

import it.unipi.MySmartRecipeBook.dto.PopularIngredientsDTO;
import it.unipi.MySmartRecipeBook.model.Neo4j.ChefNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;

public interface ChefNeo4jRepository extends Neo4jRepository<ChefNeo4j, String> {
    @Query("MATCH (c:Chef)-[:WROTE]->(r:Recipe)<-[:USED_IN]-(i:Ingredient) " +
            "WHERE NOT toLower(i.name) IN ['salt', 'water', 'pepper', 'baking soda', 'baking powder', 'olive oil', 'oil'] " +
            "RETURN c.name AS chefName, c.surname AS chefSurname, i.name AS ingredientName, count(r) AS usageCount " +
            "ORDER BY usageCount DESC " +
            "LIMIT 50")
    List<PopularIngredientsDTO> getPopularIngredientsStats();
}

