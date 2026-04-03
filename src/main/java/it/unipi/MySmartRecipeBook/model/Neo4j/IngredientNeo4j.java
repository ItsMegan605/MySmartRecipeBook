package it.unipi.MySmartRecipeBook.model.Neo4j;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import lombok.Data;

@Data
@Node("Ingredients") // Usiamo "Ingredients" per coerenza con le etichette del grafo
public class  IngredientNeo4j {

    @Id
    @GeneratedValue // Rimosso @GeneratedValue per usare l'ID manuale/condiviso
    private Long id;
    private String name;
}
