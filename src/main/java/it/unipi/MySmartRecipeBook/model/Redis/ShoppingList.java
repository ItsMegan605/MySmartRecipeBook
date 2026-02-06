package it.unipi.MySmartRecipeBook.model.Redis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Rimosso @RedisHash e @Id
public class ShoppingList implements Serializable {

    private Integer id;
    private List<String> items;

    public ShoppingList() {
        this.items = new ArrayList<>();
    }

    public ShoppingList(Integer id) {
        this.id = id;
        this.items = new ArrayList<>();
    }

    // Getter e Setter rimangono invariati...
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public List<String> getItems() { return items; }
    public void setItems(List<String> items) { this.items = items; }

    public void addItem(String item) { this.items.add(item); }
    public void removeItem(String item) { this.items.remove(item); }
}