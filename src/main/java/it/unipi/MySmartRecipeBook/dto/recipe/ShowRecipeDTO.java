package it.unipi.MySmartRecipeBook.dto.recipe;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.unipi.MySmartRecipeBook.dto.IngredientDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDate;
import java.util.List;

/**
 * DTO to filter the recipes
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShowRecipeDTO  {

    @JsonProperty("mongo_id")
    private String mongoId;

    private String title;

    @JsonProperty("image_url")
    private String imageURL;
    private String category;
    private String difficulty;

    @JsonProperty("prep_time")
    private String prepTime;

    private String presentation;
    private List<IngredientDTO> ingredients;
    private String preparation;

    private String chef;

   /* @JsonProperty("chef_id")
    private String chefId;
    @JsonProperty("creation_date")
    private LocalDate creationDate; */
}