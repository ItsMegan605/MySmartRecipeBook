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
public class AdminPendingRecipe extends PendingRecipe {

    private ReducedChef chef;

}

