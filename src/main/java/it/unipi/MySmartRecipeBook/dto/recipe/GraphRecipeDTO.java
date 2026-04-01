package it.unipi.MySmartRecipeBook.dto.recipe;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.unipi.MySmartRecipeBook.dto.IngredientDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Recipe's preview for NEO4j
 * it includes the category for queries requirements
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class GraphRecipeDTO {

    String id;
    String title;

    @JsonProperty("chef_id")
    String chefId;

    @JsonProperty("image_url")
    String imgURL;

    String category;

    List<IngredientDTO> ingredients;
}
