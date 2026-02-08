package it.unipi.MySmartRecipeBook.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import it.unipi.MySmartRecipeBook.model.Mongo.Ingredient;

@Getter
@AllArgsConstructor
public class RecipeResponseDTO {

    private String id;
    private String title;
    private String description;
    private String category;
    private String prepTime;
    private String difficulty;
    private String imageURL;
    private String preparation;
    private List<Ingredient> ingredients;
    private String chefUsername;
}
