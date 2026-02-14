package it.unipi.MySmartRecipeBook.service;
/*
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.MySmartRecipeBook.model.Mongo.Ingredient;
import it.unipi.MySmartRecipeBook.model.Redis.ShoppingList;
import it.unipi.MySmartRecipeBook.repository.FoodieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCluster;
import it.unipi.MySmartRecipeBook.model.enums.Ingredients;


@Service
public class ShoppingListService {

    @Autowired
    private JedisCluster jedisCluster; // Utilizzo diretto del cluster
    @Autowired
    private FoodieRepository foodieRepository; // Repository per MongoDB

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String REDIS_KEY_PREFIX = "shoppingList:";

    public void saveShoppingList(ShoppingList list) {
        try {
            String json = objectMapper.writeValueAsString(list);
            // JedisCluster gestisce internamente il pool e la connessione
            jedisCluster.set(REDIS_KEY_PREFIX + list.getId(), json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public ShoppingList getShoppingList(String username) {
        String json = jedisCluster.get(REDIS_KEY_PREFIX + username);
        if (json != null) {
            try {
                return objectMapper.readValue(json, ShoppingList.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return new ShoppingList(username);
    }


    public ShoppingList addIngredient(String username, String ingredient) {
        if (!Ingredients.IngredientName.isValid(ingredient)) {
            throw new IllegalArgumentException("The ingredient: " + ingredient + " is not allowed!");
        }
        if (!foodieRepository.existsByUsername(username)) {
            throw new RuntimeException("User not found");
        }
        ShoppingList list = getShoppingList(username);
        list.addItem(ingredient);
        saveShoppingList(list);
        return list;
    }

    public ShoppingList removeIngredient(String username, String ingredient) {
        if (!foodieRepository.existsByUsername(username)) {
            throw new RuntimeException("User not found");
        }
        ShoppingList list = getShoppingList(username);
        list.removeItem(ingredient); //chiamo la funzione che rimuove con case sensitive a regola
        //è la funzione del model
        saveShoppingList(list);
        return list;
    }

}
*/