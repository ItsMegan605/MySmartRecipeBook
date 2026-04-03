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

    /**
     * Internal Neo4j identifier (auto-generated).
     */
    @Id @GeneratedValue
    private Long neo4jId;

    /**
     * Reference to the corresponding MongoDB Recipe ID.
     */
    @Property("mongo_id")
    private String mongoId;

    /**
     * Title of the recipe.
     */
    private String title;

    /**
     * URL of the recipe image.
     */
    private String imageURL;

    /**
     * Category of the recipe.
     */
    private String category;

    /**
     * Relationship with ingredients.
     * Indicates which ingredients are used in this recipe.
     * Incoming relationship: Ingredient → Recipe.
     */
    @Relationship(type = "USED_IN", direction = Relationship.Direction.INCOMING)
    private List<IngredientNeo4j> ingredients;

    /**
     * Relationship with the chef who created the recipe.
     * Outgoing relationship: Recipe → Chef.
     */
    @Relationship(type = "WRITTEN_BY", direction = Relationship.Direction.OUTGOING)
    private ChefNeo4j chef;
}