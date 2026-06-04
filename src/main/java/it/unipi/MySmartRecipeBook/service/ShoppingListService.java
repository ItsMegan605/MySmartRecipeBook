package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.IngredientsListDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Foodie;
import it.unipi.MySmartRecipeBook.repository.Mongo.FoodieRepository;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Shopping List service that handles shopping list's business logic operations
 */
@Service
public class ShoppingListService {

    private final JedisSentinelPool jedisSentinelPool;
    private final IngredientService ingredientService;
    private final FoodieRepository foodieRepository;

    public ShoppingListService(JedisSentinelPool jedisSentinelPool, IngredientService ingredientService, FoodieRepository foodieRepository) {
        this.jedisSentinelPool = jedisSentinelPool;
        this.ingredientService = ingredientService;
        this.foodieRepository = foodieRepository;
    }

    private static final String REDIS_ENTITY = "Foodie:";
    private static final String REDIS_KEY_PREFIX = ":shoppingList";

    /**
     * Retrieves the shopping list of the authenticated foodie.
     * @return an {@link IngredientsListDTO} containing the set of ingredients that constitute the foodie's shopping list
     * @throws NoSuchElementException if the authenticated foodie is not found in the database
     */
    public IngredientsListDTO getShoppingList() {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Foodie foodie = foodieRepository.findByUsername(authFoodie.getUsername())
                .orElseThrow(() -> new NoSuchElementException("Foodie not found!"));
        return returnShoppingList(foodie.getUsername());
    }


    /**
     * Auxiliary method that builds the corresponding foodie's key and retrieves the shopping list from Redis.
     * @param username the unique username of the target foodie
     * @return an {@link IngredientsListDTO} containing the set of ingredients that constitute the foodie's shopping list
     */
    private IngredientsListDTO returnShoppingList(String username) {

        String key = REDIS_ENTITY + username + REDIS_KEY_PREFIX;

        Set<String> ingredients;
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            ingredients = jedis.smembers(key);
        }
        IngredientsListDTO ingredientsListDTO = new IngredientsListDTO();
        ingredientsListDTO.setIngredients(ingredients);

        return ingredientsListDTO;
    }


    /**
     * Adds a list of ingredients to the foodie's shopping list. For each element of the list, we verify that it is
     * one of the allowed ingredients in the application: if not, the element is ignored, and we only add the valid ones.
     * @param ingredients the list of ingredients to add
     * @return an {@link IngredientsListDTO} containing the foodie's shopping list appropriately updated
     * @throws NoSuchElementException if the foodie is not found
     * @throws IllegalArgumentException if the list is null or empty, or if none of the inserted ingredients are valid
     */
    public IngredientsListDTO addIngredients(List<String> ingredients) {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Foodie foodie = foodieRepository.findByUsername(authFoodie.getUsername())
                .orElseThrow(() -> new NoSuchElementException("Foodie not found!"));

        if(ingredients == null ||  ingredients.isEmpty()) {
            throw new IllegalArgumentException("No ingredient inserted");
        }

        ingredients.removeIf(ingredient -> !ingredientService.isValidIngredient(ingredient));
        ingredients.replaceAll(String::toLowerCase);

        String key = REDIS_ENTITY + foodie.getUsername() + REDIS_KEY_PREFIX;

        if (!ingredients.isEmpty()) {
            try (Jedis jedis = jedisSentinelPool.getResource()) {
                jedis.sadd(key, ingredients.toArray(new String[0]));
                jedis.expire(key, 86400*15);
            }
        }
        else{
            throw new IllegalArgumentException("No valid ingredient inserted");
        }

        return returnShoppingList(authFoodie.getUsername());
    }


    /**
     * Removes an ingredient from the foodie's shopping list. First of all, we verify that it is one of the allowed
     * ingredients in the application: if not, the element is ignored otherwise it is removed from the list.
     * @param ingredient the name of the ingredient to remove
     * @return an {@link IngredientsListDTO} containing the foodie's shopping list appropriately updated
     * @throws NoSuchElementException if the foodie is not found
     */
    public IngredientsListDTO removeIngredient(String ingredient) {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Foodie foodie = foodieRepository.findByUsername(authFoodie.getUsername())
                .orElseThrow(() -> new NoSuchElementException("Foodie not found!"));

        if(ingredientService.isValidIngredient(ingredient.toLowerCase())) {
            String key = REDIS_ENTITY + foodie.getUsername() + REDIS_KEY_PREFIX;
            try (Jedis jedis = jedisSentinelPool.getResource()) {
                jedis.srem(key, ingredient.toLowerCase());
                jedis.expire(key, 86400*15);
            }
        }
        return returnShoppingList(foodie.getUsername());
    }

}
