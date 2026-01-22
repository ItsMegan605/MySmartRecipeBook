package it.unipi.MySmartRecipeBook.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import java.util.ArrayList;
import java.util.List;

@RedisHash("shoppingList")
public class ShoppingList {

    @Id // <--- ECCOLO QUI
    private Integer id; // Questo sarà l'ID dell'utente (userId)

    private List<String> items; // La lista degli ingredienti da comprare

    // Costruttore vuoto (necessario per Spring/Redis)
    public ShoppingList() {
        this.items = new ArrayList<>();
    }

    // Costruttore con ID
    public ShoppingList(Integer id) {
        this.id = id;
        this.items = new ArrayList<>();
    }

    // Getter e Setter
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    // Metodo di utilità per aggiungere ingredienti
    public void addItem(String item) {
        this.items.add(item);
    }

    // Metodo di utilità per rimuovere ingredienti
    public void removeItem(String item) {
        this.items.remove(item);
    }
}
