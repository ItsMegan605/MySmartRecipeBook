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
 * This model includes full recipe details along with
 * the chef who submitted it.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PendingRecipe{

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

    private ReducedChef chef;

    @Field("creation_date")
    private LocalDateTime creationDate;
}