package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.IngredientsListDTO;


import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import redis.clients.jedis.JedisCluster;


import java.util.List;
import java.util.Set;

/**
 *
 */
@Service
public class ShoppingListService {

    private JedisCluster jedisCluster;
    private IngredientService ingredientService;

    public ShoppingListService(JedisCluster jedisCluster, IngredientService ingredientService) {
        this.jedisCluster = jedisCluster;
        this.ingredientService = ingredientService;
    }


    public static final String REDIS_APP_NAMESPACE = "MySmartRecipeBook:";
    private static final String REDIS_KEY_PREFIX = "shoppingList:user:";

    /**
     *
     * @return
     */
    public IngredientsListDTO getShoppingList() {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return returnShoppingList(authFoodie.getUsername());
    }

    /**
     *
     * @param username
     * @return
     */
    private IngredientsListDTO returnShoppingList(String username) {

        String key = REDIS_APP_NAMESPACE + REDIS_KEY_PREFIX + username;

        Set<String> ingredients = jedisCluster.smembers(key);
        IngredientsListDTO ingredientsListDTO = new IngredientsListDTO();
        ingredientsListDTO.setIngredients(ingredients);

        return ingredientsListDTO;
    }


    /*--------------- Add ingredients to foodie shopping list  ----------------*/

    /**
     *
     * @param ingredients
     * @return
     */
    public IngredientsListDTO addIngredients(List<String> ingredients) {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if(ingredients == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No ingredient inserted");
        }

        ingredients.removeIf(ingredient -> !ingredientService.isValidIngredient(ingredient));
        // In questo modo tutti gli ingredienti vengono sempre inseriti in minuscolo
        ingredients.replaceAll(String::toLowerCase);

        String key = REDIS_APP_NAMESPACE + REDIS_KEY_PREFIX + authFoodie.getUsername();


        // Metodo di aggiunta univoco, degli elementi alla lista - controllo se ci sono ingredienti sennò mi rispsparmio
        // la connessione a Redis

        if (!ingredients.isEmpty()) {
            jedisCluster.sadd(key, ingredients.toArray(new String[0]));
        }
        else{
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No valid ingredient inserted");
        }

        return returnShoppingList(authFoodie.getUsername());
    }


    /*--------------- Remove ingredient from foodie shopping list  ----------------*/

    /**
     *
     * @param ingredient
     * @return
     */
    public IngredientsListDTO removeIngredient(String ingredient) {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();


        if(ingredientService.isValidIngredient(ingredient.toLowerCase())) {
            String key = REDIS_APP_NAMESPACE + REDIS_KEY_PREFIX + authFoodie.getUsername();
            jedisCluster.srem(key, ingredient.toLowerCase());
        }

        return returnShoppingList(authFoodie.getUsername());
    }

}
