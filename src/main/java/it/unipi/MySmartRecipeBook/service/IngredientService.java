package it.unipi.MySmartRecipeBook.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCluster;


/**
 * Service for the ingredients
 */
@Service
public class IngredientService {
    @Autowired
    private JedisCluster jedisCluster;
    private static final String INGREDIENTS_REDIS_KEY = "MySmartRecipeBook:allowed_ingredients";


    /**
     * This method is to ensure the validity of the ingredients inserted
     * @param ingredientName - the ingredient name
     * @return true if the ingredient is valid, false otherwise
     */
    public boolean isValidIngredient(String ingredientName) {
        if (ingredientName == null || ingredientName.isBlank()) {
            return false;
        }
        return jedisCluster.sismember(INGREDIENTS_REDIS_KEY, ingredientName.toLowerCase().trim());
    }
}