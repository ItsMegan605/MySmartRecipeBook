package it.unipi.MySmartRecipeBook.dto.recipe;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecipeMatchDTO {

    private String title;
    private Integer matches;
}
