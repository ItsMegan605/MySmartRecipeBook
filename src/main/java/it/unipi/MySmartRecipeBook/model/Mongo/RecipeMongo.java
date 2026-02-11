package it.unipi.MySmartRecipeBook.model.Mongo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "recipes")
public class RecipeMongo {

    @Id
    private String id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("presentation")
    private String description;

    @JsonProperty("category")
    private String category;

    @JsonProperty("prep_Time")
    private String prepTime;

    @JsonProperty("preparation")
    private String preparation;

    @JsonProperty("difficulty")
    private String difficulty;

    @JsonProperty("image_url")
    private String imageURL;

    @JsonProperty("chef")
    private String chefUsername;

    @JsonProperty("ingredients")
    private List<Ingredient> ingredients;

    @JsonProperty("creation_date")
    private LocalDateTime creationDate;

}
