package it.unipi.MySmartRecipeBook.utils.enums;

import java.util.List;

public class Categories {

    // Static perchè così sono globali
    public static final List<String> CATEGORIES = List.of(
            "vegan", "dairy-free", "egg-free", "gluten-free",
            "main-course", "second-course", "dessert"
    );

    public static final List<String> DIFFICULTIES = List.of(
            "very easy", "easy", "average", "hard", "very hard"
    );

    public static final List<String> FOODIE_FILTERS = List.of(
            "vegan", "dairy-free", "egg-free", "gluten-free",
            "main-course", "second-course", "dessert",
            "very easy", "easy", "average", "hard", "very hard",
            "saving-date"
    );

    public static final List<String> CHEF_FILTERS = List.of(
            "date", "popularity"
    );
}
