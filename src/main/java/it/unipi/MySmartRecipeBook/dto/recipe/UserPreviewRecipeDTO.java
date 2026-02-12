package it.unipi.MySmartRecipeBook.dto.recipe;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserPreviewRecipeDTO {

    @JsonProperty("mongo_id")
    private String mongoId;
    private String title;

    private String presentation;

    @JsonProperty("image_url")
    private String imageURL;

    @JsonProperty("chef_name")
    private String chefName;
}
