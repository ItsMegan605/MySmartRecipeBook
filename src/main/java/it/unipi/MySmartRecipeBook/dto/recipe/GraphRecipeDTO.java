package it.unipi.MySmartRecipeBook.dto.recipe;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.unipi.MySmartRecipeBook.model.Mongo.Ingredient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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

    List<Ingredient> ingredients;
}
