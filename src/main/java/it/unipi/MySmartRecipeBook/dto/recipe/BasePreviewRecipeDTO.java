package it.unipi.MySmartRecipeBook.dto.recipe;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.unipi.MySmartRecipeBook.model.Mongo.Ingredient;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

//DTO for recipe's preview, abstract

@Getter
@Setter
public abstract class BasePreviewRecipeDTO {

    @JsonProperty("mongo_id")
    private String mongoId;

    private String title;

    @JsonProperty("image_URL")
    private String imageURL;

    private String presentation;

}

