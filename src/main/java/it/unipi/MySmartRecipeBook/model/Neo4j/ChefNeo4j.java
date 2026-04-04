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
 *
 * This entity is used to model chefs in the graph layer,
 * maintaining a reference to the corresponding MongoDB document.
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