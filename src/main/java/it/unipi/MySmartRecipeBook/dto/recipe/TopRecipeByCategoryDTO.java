package it.unipi.MySmartRecipeBook.dto.recipe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for the top recipe by category
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopRecipeByCategoryDTO extends UserPreviewRecipeDTO {

    private String category;
}
