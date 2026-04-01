package it.unipi.MySmartRecipeBook.dto.recipe;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;


/**
 * DTO for the foodie's recipe preview
 * NOTE: the preview of a recipe has just the id, image,chef's name and his/her id
 */

@Getter
@Setter
public class FoodiePreviewRecipeDTO {

    @JsonProperty("id")
    private String id;

    private String title;

    @JsonProperty("image_URL")
    private String imageURL;

    @JsonProperty("chef_name")
    private String chefName;

    @JsonProperty("chef_id")
    private String chefId;
}

