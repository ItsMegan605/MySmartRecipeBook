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

    public boolean isValidQuantity(){
        if(this.quantity == null || this.quantity.isEmpty()){
            return false;
        }
        return true;
    }
}
