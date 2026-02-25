package it.unipi.MySmartRecipeBook.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.MySmartRecipeBook.dto.IngredientsListDTO;


import it.unipi.MySmartRecipeBook.repository.FoodieRepository;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCluster;

import java.util.List;
import java.util.Set;


@Service
public class ShoppingListService {

    private JedisCluster jedisCluster; // Utilizzo diretto del cluster
    private IngredientService ingredientService;

    public ShoppingListService(JedisCluster jedisCluster, IngredientService ingredientService) {
        this.jedisCluster = jedisCluster;
        this.ingredientService = ingredientService;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();
    public static final String REDIS_APP_NAMESPACE = "MySmartRecipeBook";
    private static final String REDIS_KEY_PREFIX = "shoppingList:user:";


    public IngredientsListDTO getShoppingList() {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return returnShoppingList(authFoodie.getUsername());
    }

    private IngredientsListDTO returnShoppingList(String username) {

        String key = REDIS_APP_NAMESPACE + REDIS_KEY_PREFIX + username;

        Set<String> ingredients = jedisCluster.smembers(key);
        IngredientsListDTO ingredientsListDTO = new IngredientsListDTO();
        ingredientsListDTO.setIngredients(ingredients);

        return ingredientsListDTO;
    }
/*
    public void saveShoppingList(ShoppingList list) {
        try {
            String json = objectMapper.writeValueAsString(list);
            // JedisCluster gestisce internamente il pool e la connessione
            jedisCluster.set(REDIS_KEY_PREFIX + list.getId(), json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
*/


    /*--------------- Add ingredients to foodie shopping list  ----------------*/

    public IngredientsListDTO addIngredients(List<String> ingredients) {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if(ingredients == null) {
            throw new RuntimeException("No ingredients inserted");
        }
        ingredients.removeIf(ingredient -> !ingredientService.isValidIngredient(ingredient));

        String key = REDIS_APP_NAMESPACE + REDIS_KEY_PREFIX + authFoodie.getUsername();

        // Metodo di aggiunta univoco, degli elementi alla lista - controllo se ci sono ingredienti sennò mi rispsparmio
        // la connessione a Redis
        if (!ingredients.isEmpty()) {
            jedisCluster.sadd(key, ingredients.toArray(new String[0]));
        }

        return returnShoppingList(authFoodie.getUsername());
    }


    /*--------------- Remove ingredient from foodie shopping list  ----------------*/

    public IngredientsListDTO removeIngredient(String ingredient) {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        IngredientsListDTO list = returnShoppingList(authFoodie.getUsername());

        if(ingredientService.isValidIngredient(ingredient)) {
            String key = REDIS_APP_NAMESPACE + REDIS_KEY_PREFIX + authFoodie.getUsername();
            jedisCluster.srem(key, ingredient);
        }

        return list;
    }

}
