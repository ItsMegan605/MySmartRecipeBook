package it.unipi.MySmartRecipeBook.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Dto for ingredients suggestion
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IngredientSuggestionDTO {

    private String originalIngredient;

    private List<String> suggestedIngredients;
}