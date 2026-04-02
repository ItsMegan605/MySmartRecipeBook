package it.unipi.MySmartRecipeBook.repository.Neo4j;

import it.unipi.MySmartRecipeBook.dto.PopularIngredientsDTO;
import it.unipi.MySmartRecipeBook.dto.users.TopChefDTO;
import it.unipi.MySmartRecipeBook.model.Neo4j.ChefNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChefNeo4jRepository extends Neo4jRepository<ChefNeo4j, Long> {



    @Query("MATCH (c:Chef)-[:WROTE]->(r:Recipe)<-[:USED_IN]-(i:Ingredient) " +
            "WHERE NOT toLower(i.name) IN $filteredIngredients " +
            "WITH c, i, count(r) AS usageCount " +
            "ORDER BY usageCount DESC " +
            "WITH c, collect({name: i.name, count: usageCount})[0] AS topIngredient " +
            "RETURN c.name AS chefName, c.surname AS chefSurname, topIngredient.name AS ingredientName, topIngredient.count AS usageCount")
    List<PopularIngredientsDTO> getPopularIngredientsStats(List<String> filteredIngredients);

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

//prende tutti gli ingredienti di quel singolo Chef e li impacchetta in una lista
//e salva l'emeno posto a 0 come primo elemento e lo salva come top ingredient

