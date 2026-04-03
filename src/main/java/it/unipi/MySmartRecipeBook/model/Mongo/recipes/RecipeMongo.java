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
 * Represents a recipe stored in the MongoDB "recipes" collection.
 *
 * This is the main model used to store full recipe information,
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Document(collection = "recipes")

@CompoundIndexes({
        // Index to filter by Chef (useful for queries on chef-specific recipes)
        //@CompoundIndex(name = "chefDate_idx", def = "{'chef.id': 1, 'creation_date': -1}"),

        // Index to filter by Chef and sort by popularity (number of saves)
        //@CompoundIndex(name = "chefPopularity_idx", def = "{'chef.id': 1, 'num_saves': -1}"),

        @CompoundIndex(name = "chef_idx", def = "{'chef.id': 1}")
})

public class RecipeMongo {

    /**
     * Unique identifier of the recipe (MongoDB document ID).
     */
    @Id
    private String id;

    /**
     * Title of the recipe.
     */
    private String title;

    /**
     * Short presentation/description of the recipe.
     */
    @Field("presentation")
    private String presentation;

    /**
     * Category of the recipe.
     */
    @Indexed
    private String category;

    /**
     * Preparation time of the recipe.
     */
    @Field("prep_time")
    private String prepTime;

    /**
     * Full preparation instructions.
     */
    private String preparation;

    /**
     * Difficulty level of the recipe.
     */
    private String difficulty;

    /**
     * URL of the recipe image.
     */
    @Field("image_url")
    private String imageURL;

    /**
     * List of ingredients used in the recipe.
     */
    private List<RecipeIngredient> ingredients;

    /**
     * Reference to the chef who created the recipe.
     */
    private ReducedChef chef;

    /**
     * Date and time when the recipe was created.
     */
    @Field("creation_date")
    private LocalDateTime creationDate;

    /**
     * Number of times the recipe has been saved by users.
     */
    @Field("num_saves")
    private Integer numSaves;
}