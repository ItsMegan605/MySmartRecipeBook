package it.unipi.MySmartRecipeBook.model.Neo4j;

import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

/**
 * Represents an Ingredient node in the Neo4j graph database.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Node("Ingredient") // Using "Ingredients" for consistency with graph labels
public class IngredientNeo4j {

    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class) // Auto-generated ID
    private String id;

    private String name;
}