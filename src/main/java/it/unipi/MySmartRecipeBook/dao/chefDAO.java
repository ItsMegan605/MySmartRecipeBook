package it.unipi.MySmartRecipeBook.dao;


//in generale il Dao serve come intermediario tra il db e java
//
public interface chefDAO {
    void registerChef(Chef chef);
    Chef loginChef(String username, String password);

    void addRecipe(Recipe recipe);

    //mettere le altre cose che fa lo chef


}
