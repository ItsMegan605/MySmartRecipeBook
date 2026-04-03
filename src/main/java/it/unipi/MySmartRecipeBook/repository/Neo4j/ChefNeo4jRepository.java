package it.unipi.MySmartRecipeBook.repository.Neo4j;

import it.unipi.MySmartRecipeBook.dto.PopularIngredientsDTO;
import it.unipi.MySmartRecipeBook.dto.users.TopChefDTO;
import it.unipi.MySmartRecipeBook.model.Neo4j.ChefNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for managing Chef nodes in Neo4j.
 *
 * Provides methods for graph-based analytics such as:
 * - most used ingredients per chef
 * - top chefs per category
 */
public interface ChefNeo4jRepository extends Neo4jRepository<ChefNeo4j, Long> {

    /**
     * Retrieves the most used ingredient for each chef,
     * excluding a list of filtered ingredients.
     *
     * Query logic:
     * - match chefs, recipes, and ingredients
     * - exclude filtered ingredients
     * - count how many times each ingredient is used
     * - select the most used ingredient (top 1)
     *
     * @param filteredIngredients list of ingredients to exclude (lowercase)
     * @return list of PopularIngredientsDTO
     */
    @Query("MATCH (c:Chef)-[:WROTE]->(r:Recipe)<-[:USED_IN]-(i:Ingredient) " +
            "WHERE NOT toLower(i.name) IN $filteredIngredients " +
            "WITH c, i, count(r) AS usageCount " +
            "ORDER BY usageCount DESC " +
            "WITH c, collect({name: i.name, count: usageCount})[0] AS topIngredient " +
            "RETURN c.name AS chefName, c.surname AS chefSurname, topIngredient.name AS ingredientName, topIngredient.count AS usageCount")
    List<PopularIngredientsDTO> getPopularIngredientsStats(List<String> filteredIngredients);

    /**
     * Retrieves the top 3 chefs for each given category.
     *
     * Query logic:
     * - filter recipes by category
     * - group by category
     * - for each category:
     *   - count recipes per chef
     *   - sort descending
     *   - take top 3
     *
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

/*
 * This query:
 * - collects all ingredients used by a chef
 * - sorts them by usage count
 * - takes the first element (most used ingredient)
 * - returns it as the top ingredient
 */