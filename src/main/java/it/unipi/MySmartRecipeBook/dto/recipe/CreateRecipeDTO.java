package it.unipi.MySmartRecipeBook.dto.recipe;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.unipi.MySmartRecipeBook.dto.IngredientDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO for recipe's creation with mandatory fields
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateRecipeDTO{

    @NotBlank(message = "Insert title")
    private String title;

    @NotBlank (message = "Insert image URL")
    @JsonProperty("image_URL")
    private String imageURL;

    @NotBlank(message = "Insert category")
    private String category;

    @NotBlank(message = "Insert difficulty")
    private String difficulty;

    @NotBlank(message = "Insert preparation time")
    @JsonProperty("prep_time")
    private String prepTime;

    @NotBlank(message = "Insert presentation")
    @JsonProperty("presentation")
    private String presentation;

    @NotNull(message = "Insert ingredients")
    private List<IngredientDTO> ingredients;

    @NotBlank(message = "Insert preparation")
    private String preparation;

}
