package it.unipi.MySmartRecipeBook.model.Mongo.recipes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * Represents a summary view of a chef's recipe.
 *
 * This model contains only essential information and
 * it is used in the chef personal page
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChefRecipeSummary {

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
     * URL of the recipe image.
     */
    @Field("image_url")
    private String imageURL;

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