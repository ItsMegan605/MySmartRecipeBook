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
public abstract class BaseRecipeDTO {

    private String title;

    @JsonProperty("image_url")
    private String imageURL;
    private String category;
    private String difficulty;

    @JsonProperty("prep_time")
    private String prepTime;

    @JsonProperty("presentation")
    private String description;
    private List<Ingredient> ingredients;
    private String preparation;
    private String chef;

}