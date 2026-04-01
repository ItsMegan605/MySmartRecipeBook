package it.unipi.MySmartRecipeBook.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for ingredient's format
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IngredientDTO {
    private String name;
    private String quantity;
}
