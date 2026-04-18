package it.unipi.MySmartRecipeBook.model.Neo4j;

import org.springframework.data.neo4j.core.schema.*;
import lombok.Data;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.util.List;

/**
 * Represents a Recipe node in the Neo4j graph database with the
 * relationship between it and chefs and ingredients
 */
@Data
@Node("Recipe")
public class RecipeNeo4j {

    @Id @GeneratedValue(generatorClass = UUIDStringGenerator.class)
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