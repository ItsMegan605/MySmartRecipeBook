package it.unipi.MySmartRecipeBook.dto.recipe;

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

    private String mongo_id;
    private String title;
    private String description;
    private String imageURL;
    private LocalDate creationDate;
}
