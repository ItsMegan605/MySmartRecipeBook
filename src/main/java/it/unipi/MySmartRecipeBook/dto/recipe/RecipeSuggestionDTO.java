package it.unipi.MySmartRecipeBook.dto.recipe;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipeSuggestionDTO implements Serializable {

    private String id;
    private String title;
    private String imageURL;
    private String chefName;
    //list to save ingredients for the recipe
    private List<String> matchedIngredients;




}
