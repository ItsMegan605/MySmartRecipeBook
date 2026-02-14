package it.unipi.MySmartRecipeBook.repository;
/*
import it.unipi.MySmartRecipeBook.dto.recipe.RecipeSuggestionDTO;
import it.unipi.MySmartRecipeBook.model.Neo4j.RecipeNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeNeo4jRepository extends Neo4jRepository<RecipeNeo4j, String> {

    // Esegue il match e mappa direttamente sul DTO RecipeSuggestionDTO
    @Query("MATCH (i:Ingredient)-[:USED_IN]->(r:Recipe) " +
            "WHERE i.name IN $myIngredients " +
            "WITH r, count(i) AS matchCount, collect(i.name) AS matchedIngredients " +
            "WHERE matchCount >= 3 " +
            "ORDER BY matchCount DESC " +
            "RETURN r.id AS id, " +
            "       r.title AS title, " +
            "       r.imageURL AS imageURL, " +
            "       matchCount, " +
            "       matchedIngredients")
    List<RecipeSuggestionDTO> findRecipesByIngredients(List<String> myIngredients);
}
*/