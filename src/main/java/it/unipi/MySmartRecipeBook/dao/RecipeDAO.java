package it.unipi.MySmartRecipeBook.dao;

public interface RecipeDAO {
        Recipe getRecipeById(String id);
        List<Recipe> searchRecipesByName(String name);
        List<Recipe> searchRecipesByIngredient(List<String> ingredients);
        List<Recipe> getRecipesByCategory(String category);
    //ci andrà aggiunto altro poi da definire

    }

