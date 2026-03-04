package it.unipi.MySmartRecipeBook.dto.recipe;
import it.unipi.MySmartRecipeBook.dto.users.ChefInfoDTO;
import it.unipi.MySmartRecipeBook.model.ReducedChef;
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
    private ChefInfoDTO chef;
    private int matchCount; // Numero di ingredienti che fanno match
    private List<String> matchedIngredients; // Elenco dei nomi degli ingredienti trovati
}
