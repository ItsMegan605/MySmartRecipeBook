package it.unipi.MySmartRecipeBook.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * DTO wrapping a list (set) of ingredient names.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor

public class IngredientsListDTO {
    private Set<String> ingredients;
}