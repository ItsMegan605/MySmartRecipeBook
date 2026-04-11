package it.unipi.MySmartRecipeBook.model.Neo4j;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import lombok.Data;

/**
 * Represents an Ingredient node in the Neo4j graph database.
 */
@Data
@Node("Ingredient")
public class IngredientNeo4j {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
}