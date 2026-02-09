package it.unipi.MySmartRecipeBook.model.Redis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ShoppingList implements Serializable {

    private String id;
    private List<String> items;

    public ShoppingList() {
        this.items = new ArrayList<>();
    }

    public ShoppingList(String id) {
        this.id = id;
        this.items = new ArrayList<>();
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public List<String> getItems() {
        return items;
    }
    public void setItems(List<String> items) {
        this.items = items;
    }

    public void addItem(String item) {
        if (item == null || item.trim().isEmpty()) return;
        String trimmedItem = item.trim();
        boolean exists = items.stream().anyMatch(i -> i.equalsIgnoreCase(trimmedItem));

        if (!exists) {
            items.add(trimmedItem);
        }
    }

    public void removeItem(String item) {
        if (item == null) return;
        String trimmedItem = item.trim();
        items.removeIf(i -> i.equalsIgnoreCase(trimmedItem)); //logica per il case sensitive
    }
}

//TO-DO: forse aggiungere metodi per json