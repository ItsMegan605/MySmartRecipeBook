package it.unipi.MySmartRecipeBook.model.Mongo;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "recipes")
public class RecipeMongo {

    @Id
    private String id;

    @Indexed
    private String title;
    private String description;

    //necessarie le condizioni vedi altri model
    private String category;
    private String prepTime;
    private String preparation;
    private String difficulty;
    private String imageURL;

    @Indexed
    private String chefUsername;

    private List<Ingredient> ingredients;
}



/*
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

 */