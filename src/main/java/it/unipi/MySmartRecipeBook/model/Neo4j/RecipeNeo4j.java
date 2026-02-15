package it.unipi.MySmartRecipeBook.model.Neo4j;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;
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
    private List<IngredientsNeo4j> ingredients;
}
