package it.unipi.MySmartRecipeBook.model.Mongo.recipes;

import it.unipi.MySmartRecipeBook.model.Mongo.ingredients.RecipeIngredient;
import it.unipi.MySmartRecipeBook.model.Mongo.users.ReducedChef;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Document(collection = "recipes")

@CompoundIndexes({
        // Indice per filtrare per Chef E ordinare per Data (veloce per la "Vetrina")
        @CompoundIndex(name = "chefDate_idx", def = "{'chef.id': 1, 'creation_date': -1}"),

        // Indice per filtrare per Chef E ordinare per Like (veloce per "Most Liked")
        @CompoundIndex(name = "chefPopularity_idx", def = "{'chef.id': 1, 'num_saves': -1}")
})

// Così come viene salvata dentro il DB nella collezione ricette

public class RecipeMongo {

    @Id
    private String id;
    private String title;

    @Field("presentation")
    private String presentation;
    private String category;

    @Field("prep_time")
    private String prepTime;
    private String preparation;
    private String difficulty;

    @Field("image_url")
    private String imageURL;
    private List<RecipeIngredient> ingredients;

    private ReducedChef chef;

    @Field("creation_date")
    private LocalDateTime creationDate;

    @Field("num_saves")
    private Integer numSaves;
}
