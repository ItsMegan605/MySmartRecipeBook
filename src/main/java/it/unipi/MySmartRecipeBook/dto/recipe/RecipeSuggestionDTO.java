package it.unipi.MySmartRecipeBook.dto.recipe;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for recipe's suggestions in the smart fridge
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
// TODO: modificare questo DTO, non dovrebbe comparire l'intera ricetta ma solo l'anteprima
public class RecipeSuggestionDTO implements Serializable {

    private String id;
    private String title;
    private String imageURL;

    @JsonIgnore
    private String chefName;
    @JsonIgnore
    private String chefSurname;

    private String chef;

    @JsonProperty("chef_id")
    private String chefId;

    private int matchCount;
    private List<String> matchedIngredients;

    public String getChef() {
        if (chefName == null && chefSurname == null) {
            return null;
        }
        return chefName + " " + chefSurname;
    }

    public void setChef(String chef) {
        if (chef != null) {
            String[] parts = chef.split(" ", 2);
            this.chefName = parts[0];
            this.chefSurname = parts.length > 1 ? parts[1] : "";
        }
    }

}
