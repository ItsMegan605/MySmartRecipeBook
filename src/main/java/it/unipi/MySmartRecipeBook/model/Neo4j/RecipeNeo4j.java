package it.unipi.MySmartRecipeBook.model.Neo4j;

import it.unipi.MySmartRecipeBook.dto.users.ChefInfoDTO;
import it.unipi.MySmartRecipeBook.model.Ingredient;
import org.springframework.data.neo4j.core.schema.*;
import lombok.Data;

import java.util.List;

//recipe node
@Data
@Node("Recipe")
public class RecipeNeo4j {
    @Id @GeneratedValue
    private String neo4jId;

    @Property("mongo_id")
    private String mongoId; // Rimosso @GeneratedValue

    private String title;

    private String imageURL;

    private String category;

    @Relationship(type = "USED_IN", direction = Relationship.Direction.INCOMING)
    private List<Ingredient> ingredients;

    @Relationship(type = "WRITTEN_BY", direction = Relationship.Direction.OUTGOING)
    private ChefNeo4j chef;
}
