package it.unipi.MySmartRecipeBook.dto;

import lombok.Value;
import java.util.List;
import java.util.Map;

@Value
public class InfoToDeleteDTO {

    // Liste degli id delle ricette di cui vogliamo decrementare il numero di saves
    List<String> recipeIds;

    // Lista degli chef e dei decrementi del totSaves per ogni chef
    Map<String, Long> chefDecrements;
}
