package it.unipi.MySmartRecipeBook.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.MySmartRecipeBook.model.Redis.ShoppingList;
import it.unipi.MySmartRecipeBook.repository.ShoppingListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPooled;

@Service
public class ShoppingListService {

    @Autowired
    private JedisPooled jedis; // Bean di Jedis definito nella Config

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String REDIS_KEY_PREFIX = "shoppingList:";

    public void saveShoppingList(ShoppingList list) {
        try {
            String json = objectMapper.writeValueAsString(list);
            // Uso del comando SET di Jedis
            jedis.set(REDIS_KEY_PREFIX + list.getId(), json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public ShoppingList getShoppingList(Integer userId) {
        // Uso del comando GET di Jedis
        String json = jedis.get(REDIS_KEY_PREFIX + userId);
        if (json != null) {
            try {
                return objectMapper.readValue(json, ShoppingList.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return new ShoppingList(userId);
    }

    public void deleteShoppingList(Integer userId) {
        // Uso del comando DEL di Jedis
        jedis.del(REDIS_KEY_PREFIX + userId);
    }
}