package it.unipi.MySmartRecipeBook.model.Mongo.ingredients;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents an ingredient inside a recipe.
 *
 * It contains:
 * - the ingredient name
 * - the quantity used in the recipe
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecipeIngredient {

    private String name;
    private String quantity;
}