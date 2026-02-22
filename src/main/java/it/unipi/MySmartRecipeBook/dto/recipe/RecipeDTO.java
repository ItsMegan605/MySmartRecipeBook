package it.unipi.MySmartRecipeBook.dto.recipe;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.unipi.MySmartRecipeBook.model.Mongo.Ingredient;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RecipeDTO {

    @JsonProperty("mongo_id")
    private String mongoId;
    private String title;

    @JsonProperty("image_url")
    private String imageURL;
    private String category;
    private String difficulty;

    @JsonProperty("prep_time")
    private String prepTime;

    @JsonProperty("presentation")
    private String presentation;
    private List<Ingredient> ingredients;
    private String preparation;

    @JsonProperty("chef_name")
    private String chef;

}