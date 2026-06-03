package it.unipi.MySmartRecipeBook.dto.users;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO used to display a summary preview of a Chef.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChefPreviewDTO {
    private String id;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("tot_recipes")
    private Integer totRecipes;

    @JsonProperty("tot_saves")
    private Integer totSaves;

}
