package it.unipi.MySmartRecipeBook.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.MySmartRecipeBook.model.Redis.ShoppingList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCluster;

@Service
public class ShoppingListService {

    @Autowired
    private JedisCluster jedisCluster; // Utilizzo diretto del cluster

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

    public ShoppingList getShoppingList(Integer userId) {
        String json = jedisCluster.get(REDIS_KEY_PREFIX + userId);
        if (json != null) {
            try {
                return objectMapper.readValue(json, ShoppingList.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return new ShoppingList(userId);
    }
}