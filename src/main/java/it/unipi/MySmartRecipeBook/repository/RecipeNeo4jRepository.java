package it.unipi.MySmartRecipeBook.repository;

import it.unipi.MySmartRecipeBook.model.RecipeNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecipeNeo4jRepository extends Neo4jRepository<RecipeNeo4j, String> {

    // IL TUO CODICE "DIVERSO" ANDRÀ QUI.

    // Esempio: Se vuoi fare una cosa custom usando Cypher, usi l'annotazione @Query.
    // Non importa quanto sia strana la tua logica, la scrivi qui dentro.

    // @Query("MATCH (r:Recipe)-[:SIMILAR_TO]->(other) WHERE ... RETURN r")
    // List<RecipeNeo4j> laMiaLogicaStrana();
}

/*
// QUERY GRAFO: Trova le ricette che contengono *tutti* o *alcuni* degli ingredienti passati
    // Assumiamo che nel grafo tu abbia: (Recipe)-[:HAS_INGREDIENT]->(Ingredient)
    @Query("MATCH (r:Recipe)-[:HAS_INGREDIENT]->(i:Ingredient) " +
           "WHERE i.name IN $ingredients " +
           "RETURN r DISTINCT")
    List<RecipeNeo4j> findRecipesByIngredients(List<String> ingredients);
 */