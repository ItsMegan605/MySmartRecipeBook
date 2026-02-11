package it.unipi.MySmartRecipeBook.dto.recipe;

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

    private String mongo_id;
    private String title;
    private String description;
    private String imageURL;
    private String chefUsername;
    private LocalDate creationDate;
}
