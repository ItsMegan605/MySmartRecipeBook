package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.IngredientsListDTO;


import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisSentinelPool;


import java.util.List;
import java.util.Set;

/**
 * Shopping List service with business logic
 */
@Service
public class ShoppingListService {

    private final JedisSentinelPool jedisSentinelPool;

    private final IngredientService ingredientService;

    public ShoppingListService(JedisSentinelPool jedisSentinelPool, IngredientService ingredientService) {
        this.jedisSentinelPool = jedisSentinelPool;
        this.ingredientService = ingredientService;
    }


    public static final String REDIS_APP_NAMESPACE = "MySmartRecipeBook:";
    private static final String REDIS_KEY_PREFIX = "shoppingList:user:";

    /**
     * Method to return the shopping list and its contents
     * @return The shopping list
     */
    public IngredientsListDTO getShoppingList() {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return returnShoppingList(authFoodie.getUsername());
    }

    /**
     * Shopping list method to call it from user's profile
     * @param username - foodie's username
     * @return Ingredients of the shopping list
     */
    private IngredientsListDTO returnShoppingList(String username) {

        String key = REDIS_APP_NAMESPACE + REDIS_KEY_PREFIX + username;

        Set<String> ingredients;
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            ingredients = jedis.smembers(key);
        }
        IngredientsListDTO ingredientsListDTO = new IngredientsListDTO();
        ingredientsListDTO.setIngredients(ingredients);

        return ingredientsListDTO;
    }


    /**
     * Method to add ingredients to foodie's shopping list
     * @param ingredients - the ingredients that a foodie wants to add
     * @return the updated shopping list with the new ingredients
     */
    public IngredientsListDTO addIngredients(List<String> ingredients) {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if(ingredients == null) {
            throw new IllegalArgumentException("No ingredient inserted");
        }

        ingredients.removeIf(ingredient -> !ingredientService.isValidIngredient(ingredient));
        ingredients.replaceAll(String::toLowerCase);

        String key = REDIS_APP_NAMESPACE + REDIS_KEY_PREFIX + authFoodie.getUsername();


        if (!ingredients.isEmpty()) {
            try (Jedis jedis = jedisSentinelPool.getResource()) {
                jedis.sadd(key, ingredients.toArray(new String[0]));
            }
        }
        else{
            throw new IllegalArgumentException("No valid ingredient inserted");
        }

        return returnShoppingList(authFoodie.getUsername());
    }

    /**
     * Method to remove ingredients from foodie's shopping list
     * @param ingredient - the ingredient to be removed
     * @return - the updated shopping list
     */
    public IngredientsListDTO removeIngredient(String ingredient) {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();


        if(ingredientService.isValidIngredient(ingredient.toLowerCase())) {
            String key = REDIS_APP_NAMESPACE + REDIS_KEY_PREFIX + authFoodie.getUsername();
            try (Jedis jedis = jedisSentinelPool.getResource()) {
                jedis.srem(key, ingredient.toLowerCase());
            }
        }

        return returnShoppingList(authFoodie.getUsername());
    }

}
