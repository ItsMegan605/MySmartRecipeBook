package it.unipi.MySmartRecipeBook.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RecipeResponseDTO {

    private String id;
    private String title;
    private String category;
    private Integer prepTime;
    private String difficulty;
    private String imageURL;
    private List<String> ingredients;
    private String chefUsername;
}
