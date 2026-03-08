package it.unipi.MySmartRecipeBook.model.Mongo.ingredients;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecipeIngredient {
    private String name;
    private String quantity;
}
