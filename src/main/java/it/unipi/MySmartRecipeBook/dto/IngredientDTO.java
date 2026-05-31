package it.unipi.MySmartRecipeBook.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for an ingredient's format
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IngredientDTO {
    private String name;
    private String quantity;

    /**
     * Helper method to check ingredients quantity
     * @return - validation of the quantity
     */
    public boolean checkQuantity(){

        return (!(this.quantity == null || this.quantity.isEmpty()));
    }
}
