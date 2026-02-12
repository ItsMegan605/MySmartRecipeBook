package it.unipi.MySmartRecipeBook.dto.recipe;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.unipi.MySmartRecipeBook.model.Mongo.Ingredient;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BasePreviewRecipeDTO {

    private String title;

    @JsonProperty("image_URL")
    private String imageURL;

    @JsonProperty("presentation")
    private String description;

    private List<Ingredient> ingredients;
}

