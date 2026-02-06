package it.unipi.MySmartRecipeBook.model.Redis;

import it.unipi.MySmartRecipeBook.model.SmartFridgeIngredient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Rimosso @RedisHash("smartFridge")
public class SmartFridge implements Serializable {

    // Rimosso @Id
    private Integer id; // ID dell'utente
    private List<SmartFridgeIngredient> ingredients;

    public SmartFridge() {
        this.ingredients = new ArrayList<>();
    }

    public SmartFridge(Integer id) {
        this.id = id;
        this.ingredients = new ArrayList<>();
    }

    // Getter e Setter...
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public List<SmartFridgeIngredient> getIngredients() { return ingredients; }
    public void setIngredients(List<SmartFridgeIngredient> ingredients) { this.ingredients = ingredients; }
}