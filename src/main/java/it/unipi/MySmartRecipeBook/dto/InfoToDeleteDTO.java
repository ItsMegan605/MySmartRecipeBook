package it.unipi.MySmartRecipeBook.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * DTO containing information required for deletion operations
 * For example updating save counts when recipes are removed
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InfoToDeleteDTO {

    List<String> recipeIds;
    Map<String, List<String>> chefRecipeList;
}
