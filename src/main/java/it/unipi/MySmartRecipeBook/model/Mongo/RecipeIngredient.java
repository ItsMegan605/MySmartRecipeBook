package it.unipi.MySmartRecipeBook.model.Mongo;

import it.unipi.MySmartRecipeBook.model.Ingredient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredient extends Ingredient {

    private String name;

}
