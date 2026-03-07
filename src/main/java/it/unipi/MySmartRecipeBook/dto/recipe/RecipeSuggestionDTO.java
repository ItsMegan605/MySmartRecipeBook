package it.unipi.MySmartRecipeBook.dto.recipe;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import it.unipi.MySmartRecipeBook.dto.users.ChefInfoDTO;
import it.unipi.MySmartRecipeBook.model.ReducedChef;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipeSuggestionDTO implements Serializable {

    private String id;
    private String title;
    private String imageURL;

    @JsonIgnore
    private String chefName;
    @JsonIgnore
    private String chefSurname;

    private String chef;
    private int matchCount; // Numero di ingredienti che fanno match
    private List<String> matchedIngredients; // Elenco dei nomi degli ingredienti trovati

    public String getChef() {
        if (chefName == null && chefSurname == null) {
            return null;
        }
        return chefName + " " + chefSurname;
    }

    // Quando Jackson rilegge "chef" da Redis, ripopola chefName e chefSurname
    public void setChef(String chef) {
        if (chef != null) {
            String[] parts = chef.split(" ", 2);
            this.chefName = parts[0];
            this.chefSurname = parts.length > 1 ? parts[1] : "";
        }
    }

}
