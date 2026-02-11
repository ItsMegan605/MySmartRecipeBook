package it.unipi.MySmartRecipeBook.repository;

import it.unipi.MySmartRecipeBook.model.Neo4j.RecipeNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecipeNeo4jRepository extends Neo4jRepository<RecipeNeo4j, String> {

    // Questa query "crea" la relazione senza che sia scritta nel Model Java
    @Query("MATCH (i:Ingredient {name: $ingredientName}), (r:Recipe {title: $recipeTitle}) " +
            "MERGE (i)-[:USED_IN]->(r)")
    void linkIngredientToRecipe(String ingredientName, String recipeTitle);

    @Query("MATCH (i:Ingredient)-[:USED_IN]->(r:Recipe) " +
            "WHERE i.name IN $myIngredients " +
            "WITH r, count(i) as matches " +
            "WHERE matches >= 3 " +
            "ORDER BY matches DESC " +
            "RETURN r")
    List<RecipeNeo4j> findRecipesByIngredients(List<String> myIngredients);
}