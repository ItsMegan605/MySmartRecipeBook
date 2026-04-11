package it.unipi.MySmartRecipeBook.dto;

import lombok.Value;
import java.util.List;
import java.util.Map;

/**
 * DTO containing information required for deletion operations
 * For example updating save counts when recipes are removed
 */
@Value
public class InfoToDeleteDTO {

    // List of recipe IDs for which we want to decrease the save count
    List<String> recipeIds;

    // Map linking chefs to their respective save count decrements
    Map<String, List<String>> chefRecipeList;
}
