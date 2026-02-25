package it.unipi.MySmartRecipeBook.model.Redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ShoppingList implements Serializable {

    private String id;
    private Set<String> items;
/*
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

    public String toJson() throws JsonProcessingException{
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    public static ShoppingList fromJson(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, ShoppingList.class);
    }*/
}

//TO-DO: forse aggiungere metodi per json
