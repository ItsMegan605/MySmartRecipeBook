package it.unipi.MySmartRecipeBook.model.Neo4j;

import it.unipi.MySmartRecipeBook.model.Ingredient;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import lombok.Data;

import java.util.List;

//recipe node
@Data
@Node("Recipe")
public class RecipeNeo4j {
    @Id
    private String id; // Rimosso @GeneratedValue

    private String title;
    private String imageURL;

    @Relationship(type = "USED_IN", direction = Relationship.Direction.INCOMING)
    private List<Ingredient> ingredients;
}
