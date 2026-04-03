package it.unipi.MySmartRecipeBook.utils.parameters;

import java.util.List;

/**
 *
 */
public class Categories {

    // Static perchè così sono globali
    public static final List<String> CATEGORIES = List.of(
            "Vegan", "Dairy-free", "Egg-free", "Gluten-free",
            "Main courses", "Second courses", "Desserts"
    );

    public static final List<String> DIFFICULTIES = List.of(
            "Very easy", "Easy", "Average", "Hard", "Very hard"
    );

    public static final List<String> FOODIE_FILTERS = List.of(
            "Vegan", "Dairy-free", "Egg-free", "Gluten-free",
            "Main courses", "Second courses", "Desserts",
            "Very easy", "Easy", "Average", "Hard", "Very hard",
            "saving-date"
    );

}
