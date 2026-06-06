package it.unipi.MySmartRecipeBook.dto.recipe;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Recipe's preview for foodies
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserPreviewRecipeDTO {

    @JsonProperty("id")
    private String id;

    private String title;

    @JsonProperty("image_URL")
    private String imageURL;

    @JsonProperty("chef_name")
    private String chefName;

    @JsonProperty("chef_id")
    private String chefId;

    @JsonProperty("num_saves")
    private int numSaves;
}
