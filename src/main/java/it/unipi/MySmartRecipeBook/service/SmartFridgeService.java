package it.unipi.MySmartRecipeBook.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.MySmartRecipeBook.model.Redis.SmartFridge;
import it.unipi.MySmartRecipeBook.model.enums.Ingredients;
import it.unipi.MySmartRecipeBook.repository.FoodieRepository;
import it.unipi.MySmartRecipeBook.repository.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.repository.RecipeNeo4jRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCluster;

@Service
public class SmartFridgeService {

    @Autowired
    private JedisCluster jedisCluster;

    @Autowired
    private FoodieRepository foodieRepository;

    @Autowired
    private RecipeNeo4jRepository recipeNeo4jRepository;

    @Autowired
    private RecipeMongoRepository recipeMongoRepository;


    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String REDIS_KEY_PREFIX = "smartFridge:";

    //probabilmente aggiungere altro e NEO4j qui
    public void saveSmartFridge(SmartFridge list) {
        try {
            String json = objectMapper.writeValueAsString(list);
            // JedisCluster gestisce internamente il pool e la connessione
            jedisCluster.set(REDIS_KEY_PREFIX + list.getId(), json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public SmartFridge addItem(String userId, String ingredient) {
        if (!Ingredients.IngredientName.isValid(ingredient)) {
            throw new IllegalArgumentException("The ingredient: " + ingredient + " is not allowed!");
        }
        if (!foodieRepository.existsFoodieById(userId)) { // Usa il nuovo metodo
            throw new RuntimeException("User not found");
        }

        SmartFridge list = getSmartFridge(userId);
        list.addIngredient(ingredient);
        saveSmartFridge(list);
        return list;
    }

    public SmartFridge removeItem(String userId, String ingredient) {
        if (!foodieRepository.existsFoodieById(userId)) { //come nella add
            throw new RuntimeException("User not found");
        }
        SmartFridge list = getSmartFridge(userId);
        list.removeIngredient(ingredient);
        saveSmartFridge(list);
        return list;
    }

    public SmartFridge getSmartFridge(String userId) {
        String json = jedisCluster.get(REDIS_KEY_PREFIX + userId);
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        if (json != null) {
            try {
                return objectMapper.readValue(json, SmartFridge.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return new SmartFridge(userId);
    }

    /*
    public SmartFridge getRecipeById(String recipeId) {
        return null;
    } */

}