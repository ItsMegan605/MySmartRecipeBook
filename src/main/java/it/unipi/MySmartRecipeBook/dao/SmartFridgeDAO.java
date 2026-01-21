package it.unipi.MySmartRecipeBook.dao;

public interface SmartFridgeDAO {

    //ci vanno messe leoperaizoni "basic" che fa
    void saveFridge(SmartFridge fridge);
    SmartFridge loadFridge(Integer userId);

    // Operazioni granulari (Più efficienti su Redis)
    void addIngredient(Integer userId, String ingredient);
    void removeIngredient(Integer userId, String ingredient);
}
