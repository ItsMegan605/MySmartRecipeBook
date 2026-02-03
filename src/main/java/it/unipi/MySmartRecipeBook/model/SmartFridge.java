package it.unipi.MySmartRecipeBook.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.ArrayList;
import java.util.List;
@RedisHash("smartFridge")
public class SmartFridge {
    @Id
    private Integer userId;
    private List<SmartFridgeIngredient> ingredients;

    public SmartFridge(Integer userId) { // Il nome deve essere uguale alla Classe (era ShoppingCart)
        this.userId = userId;
        this.ingredients = new ArrayList<SmartFridgeIngredient>();
    }

    public void addProduct(Integer ingredientId, String ingredientName){ //qui poi ci dobbiamo metere le cose che ci servono
        ingredients.add(new SmartFridgeIngredient(ingredientId, ingredientName));
    }

    public void addProduct(SmartFridgeIngredient smartFridgeIngredient){
        ingredients.add(smartFridgeIngredient); //aggiungere ingredienti al frigo
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public List<SmartFridgeIngredient> getProducts() {
        return ingredients;
    }

    public void setProducts(List<SmartFridgeIngredient> ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public String toString() {
        return "SmartFridge{" +
                "userId=" + userId +
                ", products=" + ingredients +
                '}';
    }
}
