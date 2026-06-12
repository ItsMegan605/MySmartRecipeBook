package it.unipi.MySmartRecipeBook.model.Neo4j;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.util.List;

/**
 * Represents a Recipe node in the Neo4j graph database with the
 * relationship between it and chefs and ingredients
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Node("Recipe")
public class RecipeNeo4j {

    @Id @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    private Long neo4jId;

    @Property("mongo_id")
    private String mongoId;

    private String title;

    private String imageURL;

    private String category;

    private List<IngredientNeo4j> ingredients;

    private ChefNeo4j chef;
}