package it.unipi.MySmartRecipeBook.dto.recipe;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

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

    @Field("imageURL")
    @JsonProperty("image_URL")
    private String imageURL;

    @Field("chefName")
    @JsonProperty("chef_name")
    private String chefName;

    @Field("chefId")
    @JsonProperty("chef_id")
    private String chefId;

    @Field("numSaves")
    @JsonProperty("num_saves")
    private int numSaves;
}
