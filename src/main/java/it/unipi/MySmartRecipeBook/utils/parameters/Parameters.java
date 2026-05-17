package it.unipi.MySmartRecipeBook.utils.parameters;

import java.util.List;

/**
 * Parameters used globally in the codes
 */
public class Parameters {

    //static to make them global
    public static final List<String> CATEGORIES = List.of(
            "Vegan", "Dairy-free", "Egg-free", "Gluten-free",
            "Main courses", "Second courses", "Desserts", "Appetizers",
            "Drinks and beverages", "Sauces"
    );

    public static final List<String> DIFFICULTIES = List.of(
            "Very easy", "Easy", "Medium", "Difficult", "Very difficult"
    );

    public static final List<String> FOODIE_FILTERS = List.of(
            "Vegan", "Dairy-free", "Egg-free", "Gluten-free",
            "Main courses", "Second courses", "Desserts", "Appetizers",
            "Drinks and beverages", "Sauces",
            "Very easy", "Easy", "Medium", "Difficult", "Very difficult",
            "saving-date"
    );

    public static final List<String> FILTERED_INGREDIENTS = List.of(
            "salt", "water", "pepper", "oil"
    );
}
