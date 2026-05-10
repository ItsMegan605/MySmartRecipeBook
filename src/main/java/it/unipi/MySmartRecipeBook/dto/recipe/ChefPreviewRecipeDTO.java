package it.unipi.MySmartRecipeBook.dto.recipe;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Chef's recipe preview
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChefPreviewRecipeDTO{

    @JsonProperty("id")
    private String id;

    private String title;

    @JsonProperty("image_URL")
    private String imageURL;

    @JsonProperty("creation_date")
    private LocalDate creationDate;

    @JsonProperty("num_saves")
    private int numSaves;
}
