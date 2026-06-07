package it.unipi.MySmartRecipeBook.repository.Neo4j;

import it.unipi.MySmartRecipeBook.model.Neo4j.IngredientNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;

/**
 * Repository for managing Ingredient nodes in Neo4j.
 */
public interface IngredientNeo4jRepository  extends Neo4jRepository<IngredientNeo4j, String> {


}
