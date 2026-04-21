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
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a recipe stored in the MongoDB "recipes" collection with full information
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Document(collection = "recipes")
public class RecipeMongo {

    @Id
    private String id;

    @Indexed(unique = true)
    private String title;

    @Field("presentation")
    private String presentation;

    @Indexed
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