package it.unipi.MySmartRecipeBook.model.Mongo.recipes;

import it.unipi.MySmartRecipeBook.model.Mongo.ingredients.RecipeIngredient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a recipe submitted by a chef that is pending approval.
 *
 * This model is used for recipes that have not yet been
 * approved by the admin
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChefPendingRecipe{


    @Field("id")
    String id;

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

    @Field("creation_date")
    private LocalDateTime creationDate;

}