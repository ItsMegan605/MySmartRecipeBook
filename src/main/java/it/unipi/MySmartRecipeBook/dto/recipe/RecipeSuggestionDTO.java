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

    /**
     * Retrieves the full name of the chef.
     * Combines the chef's first name and surname.
     * @return The full name of the chef, or null if both the first name and surname are not set.
     */
    public String getChef() {
        if (chefName == null && chefSurname == null) {
            return null;
        }
        return chefName + " " + chefSurname;
    }

    /**
     * Sets the chef's first name and surname by parsing a single full name string.
     * The input string is split at the first space to separate the first name from the surname.
     *
     * @param chef The full name of the chef (e.g., "Massimo Bottura").
     */
    public void setChef(String chef) {
        if (chef != null) {
            String[] parts = chef.split(" ", 2);
            this.chefName = parts[0];
            this.chefSurname = parts.length > 1 ? parts[1] : "";
        }
    }

}
