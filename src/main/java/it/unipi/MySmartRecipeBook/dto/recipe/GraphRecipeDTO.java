package it.unipi.MySmartRecipeBook.dto.recipe;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.unipi.MySmartRecipeBook.dto.IngredientDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
/**
 * DTO representing a recipe for Neo4j.
 * It includes the category field to satisfy the queries.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GraphRecipeDTO {

    private String id;

    private String title;

    @JsonProperty("chef_id")
    private String chefId;

    @JsonProperty("image_url")
    private String imgURL;

    private String category;

    private List<IngredientDTO> ingredients;
}
