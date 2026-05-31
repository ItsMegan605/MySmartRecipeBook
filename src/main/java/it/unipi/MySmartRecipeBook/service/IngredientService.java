package it.unipi.MySmartRecipeBook.service;

import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;


/**
 * Service for the ingredients
 */
@Service
public class IngredientService {
    private final JedisSentinelPool jedisSentinelPool;
    private static final String INGREDIENTS_REDIS_KEY = "Allowed_ingredients";

    public IngredientService(JedisSentinelPool jedisSentinelPool) {
        this.jedisSentinelPool = jedisSentinelPool;
    }
    /**
     * This method is to ensure the validity of the ingredients inserted
     * @param ingredientName - the ingredient name
     * @return true if the ingredient is valid, false otherwise
     */
    public boolean isValidIngredient(String ingredientName) {
        if (ingredientName == null || ingredientName.isBlank()) {
            return false;
        }
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return jedis.sismember(INGREDIENTS_REDIS_KEY, ingredientName.toLowerCase().trim());
        }
    }
}