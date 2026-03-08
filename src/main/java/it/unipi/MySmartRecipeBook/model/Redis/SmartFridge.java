package it.unipi.MySmartRecipeBook.model.Redis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class SmartFridge implements Serializable {

    private String id; // ID dell'utente
    private List<String> ingredients;

    public SmartFridge() {
        this.ingredients = new ArrayList<String>();
    }

    public SmartFridge(String id) {
        this.id = id;
        this.ingredients = new ArrayList<String>();
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }


}
