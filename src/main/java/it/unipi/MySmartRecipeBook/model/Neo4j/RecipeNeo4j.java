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

    @Id // Usiamo l'ID di MongoDB, senza generatore automatico
    private String id;

    private String title;

    // Indica che la relazione arriva DA Ingredient VERSO Recipe
    @Relationship(type = "USED_IN", direction = Relationship.Direction.INCOMING)
    private List<IngredientsNeo4j> ingredients;

    // Inserisci qui solo i campi necessari per la visualizzazione rapida
    private String chefName;
}
