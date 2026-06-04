package it.unipi.MySmartRecipeBook.service;

import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.exceptions.JedisException;

import java.util.HashSet;
import java.util.Set;

/**
 * Ingredient service
 */
@Service
public class IngredientService {

    private static final String INGREDIENTS_REDIS_KEY = "Allowed_ingredients";

    private final JedisSentinelPool jedisSentinelPool;
    public IngredientService(JedisSentinelPool jedisSentinelPool) {

        this.jedisSentinelPool = jedisSentinelPool;
    }


    /**
     * Validates the provided ingredient name against the official list stored in Redis.
     * @param ingredientName the unique name of the ingredient to validate
     * @return true if the ingredient is valid, false if it is null, blank, or not found
     * @throws JedisException if the connection to the Redis server fails
     */
    public boolean isValidIngredient(String ingredientName) {
        if (ingredientName == null || ingredientName.isBlank()) {
            return false;
        }
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return jedis.sismember(INGREDIENTS_REDIS_KEY, ingredientName.toLowerCase().trim());
        }
        catch (JedisException e) {
            System.err.println("Connection to Redis failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the complete set of allowed ingredients from Redis.
     * This method is used to populate the frontend autocomplete options.
     *
     * @return a {@link Set} containing all valid ingredients, or an empty set if the connection fails
     */
    public Set<String> getAllAllowedIngredients() {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return jedis.smembers(INGREDIENTS_REDIS_KEY);
        }
        catch (JedisException e) {
            return new HashSet<>();
        }
    }
}