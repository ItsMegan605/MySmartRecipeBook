package it.unipi.MySmartRecipeBook.dto.recipe;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * DTO for the top recipe by category
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopRecipeByCategoryDTO {

    @JsonProperty("id")
    private String id;

    private String title;

    private String imageURL;

    @JsonProperty("chef_name")
    private String chefName;

    @JsonProperty("chef_id")
    private String chefId;

    @JsonProperty("num_saves")
    private int numSaves;

    private String category;
}
