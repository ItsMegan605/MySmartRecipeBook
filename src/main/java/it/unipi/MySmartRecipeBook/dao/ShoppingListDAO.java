package it.unipi.MySmartRecipeBook.dao;

public interface ShoppingListDAO {
    //aggiungi un elemento
    void addItem(Integer userId, String itemName);

    //tolgo un elemento
    void removeItem(Integer userId, String itemName);

    //prendo tutta la lista
    ShoppingList getShoppingList(Integer userId);
}
