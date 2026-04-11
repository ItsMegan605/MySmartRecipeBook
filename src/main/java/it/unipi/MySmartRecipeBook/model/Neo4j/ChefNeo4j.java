package it.unipi.MySmartRecipeBook.model.Neo4j;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

/**
 * Represents a Chef node in the Neo4j graph database.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Node("Chef")
public class ChefNeo4j {

    @Id @GeneratedValue
    private Long neo4jId;

    @Property("mongo_id")
    private String mongoId;

    private String name;

    private String surname;
}