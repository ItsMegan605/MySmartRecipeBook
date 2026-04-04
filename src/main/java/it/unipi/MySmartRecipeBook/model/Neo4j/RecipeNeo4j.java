package it.unipi.MySmartRecipeBook.model.Neo4j;

import org.springframework.data.neo4j.core.schema.*;
import lombok.Data;

import java.util.List;

/**
 * Represents a Recipe node in the Neo4j graph database.
 *
 * This entity models recipes in the graph and defines relationships
 * with ingredients and chefs.
 */
@Data
@Node("Recipe")
public class RecipeNeo4j {

    @Id @GeneratedValue
    private Long neo4jId;

    @Property("mongo_id")
    private String mongoId;

    private String title;

    private String imageURL;

    private String category;


    @Relationship(type = "USED_IN", direction = Relationship.Direction.INCOMING)
    private List<IngredientNeo4j> ingredients;

    @Relationship(type = "WRITTEN_BY", direction = Relationship.Direction.OUTGOING)
    private ChefNeo4j chef;
}