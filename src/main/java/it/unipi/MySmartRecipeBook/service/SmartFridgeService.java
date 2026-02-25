package it.unipi.MySmartRecipeBook.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.MySmartRecipeBook.dto.IngredientsListDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.RecipeSuggestionDTO;
import it.unipi.MySmartRecipeBook.model.Redis.SmartFridge;
import it.unipi.MySmartRecipeBook.repository.FoodieRepository;
import it.unipi.MySmartRecipeBook.repository.RecipeNeo4jRepository;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCluster;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class SmartFridgeService {


    private JedisCluster jedisCluster;
    private FoodieRepository foodieRepository;
    private RecipeNeo4jRepository recipeNeo4jRepository;
    private IngredientService ingredientService;

    public SmartFridgeService(JedisCluster jedisCluster, FoodieRepository foodieRepository,
                              RecipeNeo4jRepository recipeNeo4jRepository, IngredientService ingredientService){
        this.jedisCluster = jedisCluster;
        this.foodieRepository = foodieRepository;
        this.recipeNeo4jRepository = recipeNeo4jRepository;
        this.ingredientService = ingredientService;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();
    public static final String REDIS_APP_NAMESPACE = "MySmartRecipeBook";
    private static final String REDIS_FRIDGE_PREFIX = "smartFridge:items:";
    private static final String REDIS_RECIPES_PREFIX = "smartFridge:suggestions:";


    public IngredientsListDTO getSmartFridge() {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return returnSmartFridge(authFoodie.getUsername());
    }

    private IngredientsListDTO returnSmartFridge(String username) {

        String key = REDIS_APP_NAMESPACE + REDIS_FRIDGE_PREFIX + username;

        Set<String> ingredients = jedisCluster.smembers(key);
        IngredientsListDTO ingredientsListDTO = new IngredientsListDTO();
        ingredientsListDTO.setIngredients(ingredients);

        return ingredientsListDTO;
    }
/*
    public void saveSmartFridge(SmartFridge list) {
        try {
            String json = objectMapper.writeValueAsString(list);
            // JedisCluster gestisce internamente il pool e la connessione
            jedisCluster.set(REDIS_FRIDGE_PREFIX + list.getId(), json);
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
        // In questo modo tutti gli ingredienti vengono sempre inseriti in minuscolo
        ingredients.replaceAll(String::toLowerCase);

        String key = REDIS_APP_NAMESPACE + REDIS_FRIDGE_PREFIX + authFoodie.getUsername();


        // Metodo di aggiunta univoco, degli elementi alla lista - controllo se ci sono ingredienti sennò mi rispsparmio
        // la connessione a Redis
        if (!ingredients.isEmpty()) {
            jedisCluster.sadd(key, ingredients.toArray(new String[0]));
            jedisCluster.del(REDIS_RECIPES_PREFIX + authFoodie.getUsername());
        }
        else{
            System.out.println("No ingredients inserted");
        }
        return returnSmartFridge(authFoodie.getUsername());
    }


    /*--------------- Remove ingredient from foodie shopping list  ----------------*/

    public IngredientsListDTO removeIngredient(String ingredient) {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();


        if(ingredientService.isValidIngredient(ingredient.toLowerCase())) {
            String key = REDIS_APP_NAMESPACE + REDIS_FRIDGE_PREFIX + authFoodie.getUsername();
            jedisCluster.srem(key, ingredient.toLowerCase());
            updateCacheAfterRemoval(authFoodie.getUsername(), ingredient.toLowerCase());
        }

        return getSmartFridge();
    }



    public List<RecipeSuggestionDTO> getRecommendations(String username) {
        String cacheKey = REDIS_RECIPES_PREFIX + username;

        String json = jedisCluster.get(cacheKey);
        if (json != null) {
            try {
                return objectMapper.readValue(json, new TypeReference<List<RecipeSuggestionDTO>>(){});
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        //if the recipe is not already in the cache check if there are the matched 3 ingredients
        IngredientsListDTO ingredientsListDTO = getSmartFridge();
        Set<String> ingredientsSet = ingredientsListDTO.getIngredients();

        if (ingredientsSet == null || ingredientsSet.size() < 3) {
            return new ArrayList<>();
        }

        //and we check in neo4j
        List<String> ingredients = new ArrayList<>(ingredientsSet);
        List<RecipeSuggestionDTO> suggestions = recipeNeo4jRepository.findRecipesByIngredients(ingredients);

        //we get the suggestion and cache it
        if (!suggestions.isEmpty()) {
            try {
                jedisCluster.set(cacheKey, objectMapper.writeValueAsString(suggestions));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return suggestions;
    }


    private void updateCacheAfterRemoval(String username, String removedIngredient) {
        String cacheKey = REDIS_RECIPES_PREFIX + username;
        String json = jedisCluster.get(cacheKey);

        if (json != null) {
            try {
                List<RecipeSuggestionDTO> cachedRecipes = objectMapper.readValue(json, new TypeReference<List<RecipeSuggestionDTO>>(){});
                List<RecipeSuggestionDTO> updatedList = new ArrayList<>();

                for (RecipeSuggestionDTO recipe : cachedRecipes) {

                     List<String> listIngredient = recipe.getMatchedIngredients();
                     for (String ingredient : listIngredient) {
                         if (ingredient.equals(removedIngredient)) {
                             recipe.setMatchCount(recipe.getMatchCount() - 1 );
                             listIngredient.remove(ingredient);
                             break;
                         }
                     }

                    //keep the recipe just if we still have 3 matches
                    if (recipe.getMatchedIngredients().size() >= 3) {
                        updatedList.add(recipe);
                    }
                }

                if (updatedList.isEmpty()) {
                    jedisCluster.del(cacheKey);
                } else {
                    updatedList.sort(Comparator.comparingInt(RecipeSuggestionDTO::getMatchCount).reversed());
                    jedisCluster.set(cacheKey, objectMapper.writeValueAsString(updatedList));
                }

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

}
