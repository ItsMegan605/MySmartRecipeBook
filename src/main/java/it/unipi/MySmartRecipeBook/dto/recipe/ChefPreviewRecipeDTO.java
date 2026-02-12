package it.unipi.MySmartRecipeBook.dto.recipe;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChefPreviewRecipeDTO {

    @JsonProperty("mongo_id")
    private String mongoId;

    private String title;
    private String presentation;

    @JsonProperty("image_url")
    private String imageURL;

    @JsonProperty("creation_date")
    private LocalDate creationDate;
}
