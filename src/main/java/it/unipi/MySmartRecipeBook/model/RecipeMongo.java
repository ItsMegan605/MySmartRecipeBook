package it.unipi.MySmartRecipeBook.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.util.List;

//Serve a salvare tutto il contenuto "pesante" della ricetta.

@Data // Lombok genera getter, setter, toString da solo
@Document(collection = "recipes") // Dice a Mongo: "Salva questi dati nella collezione 'recipes'"
public class RecipeMongo {
    @Id
    private String id; // L'ID univoco che Mongo genera in automatico

    private String title;
    private String description;
    private String category;
    private Double prepTime;
    private String difficulty;
    private String imageURL;
    private String preparation;
    private List<String> ingredients;
    private String chefName;
}