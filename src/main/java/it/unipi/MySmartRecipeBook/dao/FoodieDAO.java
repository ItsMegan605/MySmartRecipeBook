package it.unipi.MySmartRecipeBook.dao;

public interface FoodieDAO {
    //login e registrazione
    void registerFoodie(Foodie foodie);
    Foodie loginFoodie(String username, String password);
    void updateProfile(Foodie foodie); // Es. cambio email o password

    //saves (Salvati in Mongo dentro l'oggetto Foodie o in una collezione a parte)
    void addFavoriteRecipe(String username, String recipeId);
    void removeFavoriteRecipe(String username, String recipeId);
    List<Recipe> getFavoriteRecipes(String username);

   //altre eventuali operaizoni per ora ho messo le basi
}
