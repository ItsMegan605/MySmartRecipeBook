package it.unipi.MySmartRecipeBook.repository;

import it.unipi.MySmartRecipeBook.dto.PopularIngredientsDTO;
import it.unipi.MySmartRecipeBook.dto.users.TopChefDTO;
import it.unipi.MySmartRecipeBook.model.Neo4j.ChefNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChefNeo4jRepository extends Neo4jRepository<ChefNeo4j, String> {

    @Query("MATCH (c:Chef)-[:WROTE]->(r:Recipe)<-[:USED_IN]-(i:Ingredient) " +
            "WHERE NOT toLower(i.name) IN $filteredIngredients " +
            "WITH c, i, count(r) AS usageCount " +
            "ORDER BY usageCount DESC " +
            "WITH c, collect({name: i.name, count: usageCount})[0] AS topIngredient " +
            "RETURN c.name AS chefName, c.surname AS chefSurname, topIngredient.name AS ingredientName, topIngredient.count AS usageCount")
    List<PopularIngredientsDTO> getPopularIngredientsStats(List<String> filteredIngredients);

    @Query(
            "MATCH (c:Chef)-[:WROTE]->(r:Recipe) " +
                    "WHERE r.category IN $categories " +
                    "WITH r.category AS category, c, count(r) AS recipes " +
                    "ORDER BY category, recipes DESC " +
                    "WITH category, collect({name: c.name, surname: c.surname, recipes: recipes})[0..3] AS topChefs " +
                    "RETURN category, topChefs"
    )
    List<TopChefDTO> findTop3ChefsByCategory(@Param("categories") List<String> categories);
}

//prende tutti gli ingredienti di quel singolo Chef e li impacchetta in una lista
//e salva l'emeno posto a 0 come primo elemento e lo salva come top ingredient

