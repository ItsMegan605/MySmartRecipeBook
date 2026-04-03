package it.unipi.MySmartRecipeBook.model.Mongo.recipes;

import it.unipi.MySmartRecipeBook.model.Mongo.users.ReducedChef;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;

/**
 * Represents a summary of a recipe saved by a foodie.
 *
 * This model is used to store essential information about
 * recipes saved by users
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FoodieRecipeSummary {

    /**
     * Unique identifier of the recipe.
     */
    @Field("id")
    private String id;

    /**
     * Title of the recipe.
     */
    private String title;

    /**
     * Category of the recipe.
     */
    private String category;

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
     * Date when the recipe was saved by the foodie.
     */
    @Field("saving_date")
    private LocalDate savingDate;

    /**
     * Reference to the chef who created the recipe.
     * Used when updating or decrementing the chef's saved recipes counter.
     */
    @Field("chef")
    private ReducedChef chef;
}