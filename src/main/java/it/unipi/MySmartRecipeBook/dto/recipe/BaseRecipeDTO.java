package it.unipi.MySmartRecipeBook.dto.recipe;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.unipi.MySmartRecipeBook.model.Mongo.Ingredient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class BaseRecipeDTO {

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