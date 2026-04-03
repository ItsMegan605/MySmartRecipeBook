package it.unipi.MySmartRecipeBook.model.Mongo.recipes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Represents a minimal reference to an old recipe.
 *
 * This model is used to store limited information about
 * previously created recipes in the chef profile page
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OldRecipe {

    /**
     * Unique identifier of the recipe.
     */
    @Field("id")
    String id;

    /**
     * Number of times the recipe has been saved.
     */
    @Field("num_saves")
    private Integer numSaves;
}