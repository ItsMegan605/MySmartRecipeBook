package it.unipi.MySmartRecipeBook.model.Neo4j;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;
import lombok.Data;

@Data
@Node("Recipe") // 1. Mappa questo oggetto all'etichetta (Label) :Recipe nel DB
public class RecipeNeo4j {

    @Id // 2. Definisce la chiave primaria
    @GeneratedValue(generatorClass = UUIDStringGenerator.class) // 3. Genera ID univoci in automatico
    private String id;

    // Aggiungi qui sotto solo i campi che ti servono per la tua logica "diversa"
    // (es. property specifiche per il calcolo del peso, score, ecc.)
    private String title;
}