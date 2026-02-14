package it.unipi.MySmartRecipeBook.model.Redis;
/*
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

    //metodi per le maiuscole
    public void addIngredient(String ingredient) {
        if (ingredient == null || ingredient.trim().isEmpty()) return;
        String trimmedItem = ingredient.trim();
        boolean exists = ingredients.stream().anyMatch(i -> i.equalsIgnoreCase(trimmedItem));

        if (!exists) {
            ingredients.add(trimmedItem);
        }
    }

    public void removeIngredient(String item) {
        if (item == null) return;
        String trimmedItem = item.trim();
        ingredients.removeIf(i -> i.equalsIgnoreCase(trimmedItem)); //logica per il case sensitive
    }
}
*/