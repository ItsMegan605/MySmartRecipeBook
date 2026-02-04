package it.unipi.MySmartRecipeBook.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FoodieResponseDTO {

    private String username;
    private String name;
    private String surname;
    private String email;
}
