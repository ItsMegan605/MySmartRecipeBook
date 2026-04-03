package it.unipi.MySmartRecipeBook.model.Mongo.recipes;

import it.unipi.MySmartRecipeBook.model.Mongo.ingredients.RecipeIngredient;
import it.unipi.MySmartRecipeBook.model.Mongo.users.ReducedChef;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a recipe that is pending approval.
 *
 * This model includes full recipe details along with
 * the chef who submitted it.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PendingRecipe{

    /**
     * Unique identifier of the recipe.
     */
    @Field("id")
    String id;

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
}