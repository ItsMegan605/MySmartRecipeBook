package it.unipi.MySmartRecipeBook.dto.recipe;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopRecipeByCategoryDTO {
    private String category;
    private String title;

    @JsonProperty("id")
    private String id;

    @Field("image_url")
    @JsonProperty("image_URL")
    private String imageURL;

    //@Field("chef_name")
    @JsonProperty("chef_name")
    private String chefName;

    @Field("chef_id")
    @JsonProperty("chef_id")
    private String chefId;

    @Field("num_saves")
    @JsonProperty("num_saves")
    private int numSaves;
}