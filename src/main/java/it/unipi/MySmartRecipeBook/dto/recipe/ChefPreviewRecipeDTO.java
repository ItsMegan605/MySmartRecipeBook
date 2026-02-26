package it.unipi.MySmartRecipeBook.dto.recipe;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

//chef's recipe's preview

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class ChefPreviewRecipeDTO extends PreviewRecipeDTO {

    @JsonProperty("creation_date")
    private LocalDate creationDate;

    @JsonProperty("num_saves")
    private int numSaves;
}
