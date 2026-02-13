package it.unipi.MySmartRecipeBook.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.MySmartRecipeBook.dto.recipe.RecipeSuggestionDTO;
import it.unipi.MySmartRecipeBook.model.Redis.SmartFridge;
import it.unipi.MySmartRecipeBook.model.enums.Ingredients;
import it.unipi.MySmartRecipeBook.repository.FoodieRepository;
import it.unipi.MySmartRecipeBook.repository.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.repository.RecipeNeo4jRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCluster;

import java.util.ArrayList;
import java.util.List;

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
    private static final String REDIS_FRIDGE_PREFIX = "smartFridge:items:";
    private static final String REDIS_RECIPES_PREFIX = "smartFridge:suggestions:";
    private static final int CACHE_TTL_SECONDS = 3600; // 1 ora



    public SmartFridge getSmartFridge(String username) {
        String json = jedisCluster.get(REDIS_FRIDGE_PREFIX + username);
        if (json != null) {
            try {
                return objectMapper.readValue(json, SmartFridge.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return new SmartFridge(username);
    }

    public void saveSmartFridge(SmartFridge list) {
        try {
            String json = objectMapper.writeValueAsString(list);
            // JedisCluster gestisce internamente il pool e la connessione
            jedisCluster.set(REDIS_FRIDGE_PREFIX + list.getId(), json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public SmartFridge addItem(String username, String ingredient) {
        if (!Ingredients.IngredientName.isValid(ingredient)) {
            throw new IllegalArgumentException("The ingredient: " + ingredient + " is not allowed!");
        }
        if (!foodieRepository.existsByUsername(username)) {
            throw new RuntimeException("User not found");
        }

        SmartFridge list = getSmartFridge(username);
        list.addIngredient(ingredient);
        saveSmartFridge(list);
        jedisCluster.del(REDIS_RECIPES_PREFIX + username);
        return list;
    }

    public List<RecipeSuggestionDTO> getRecommendations(String username) {
        String cacheKey = REDIS_RECIPES_PREFIX + username;

        // 1. Controllo Redis (Cache Hit)
        String json = jedisCluster.get(cacheKey);
        if (json != null) {
            try {
                return objectMapper.readValue(json, new TypeReference<List<RecipeSuggestionDTO>>(){});
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        // 2. Cache Miss: Recupero ingredienti
        SmartFridge fridge = getSmartFridge(username);
        List<String> ingredients = fridge.getIngredients();

        if (ingredients.size() < 3) {
            return new ArrayList<>();
        }

        // 3. Query Neo4j (Repository style)
        List<RecipeSuggestionDTO> suggestions = recipeNeo4jRepository.findRecipesByIngredients(ingredients);

        // 4. Cache Populate
        if (!suggestions.isEmpty()) {
            try {
                jedisCluster.set(cacheKey, objectMapper.writeValueAsString(suggestions));
                jedisCluster.expire(cacheKey, CACHE_TTL_SECONDS);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return suggestions;
    }

    public SmartFridge removeItem(String username, String ingredient) {
        if (!foodieRepository.existsByUsername(username)) {
            throw new RuntimeException("User not found");
        }
        SmartFridge list = getSmartFridge(username);
        list.removeIngredient(ingredient);
        saveSmartFridge(list);
        updateCacheAfterRemoval(username, ingredient);
        return list;
    }

    private void updateCacheAfterRemoval(String username, String removedIngredient) {
        String cacheKey = REDIS_RECIPES_PREFIX + username;
        String json = jedisCluster.get(cacheKey);

        if (json != null) {
            try {
                List<RecipeSuggestionDTO> cachedRecipes = objectMapper.readValue(json, new TypeReference<List<RecipeSuggestionDTO>>(){});
                List<RecipeSuggestionDTO> updatedList = new ArrayList<>();

                for (RecipeSuggestionDTO recipe : cachedRecipes) {
                    // Rimuovi l'ingrediente dalla lista dei match (gestione case-insensitive)
                    recipe.getMatchedIngredients().removeIf(i -> i.equalsIgnoreCase(removedIngredient));

                    // Filtro: mantengo solo se ho ancora >= 3 match
                    if (recipe.getMatchedIngredients().size() >= 3) {
                        updatedList.add(recipe);
                    }
                }

                if (updatedList.isEmpty()) {
                    jedisCluster.del(cacheKey);
                } else {
                    // Aggiorno la cache con la lista ridotta
                    jedisCluster.set(cacheKey, objectMapper.writeValueAsString(updatedList));
                    // IMPORTANTE: Reimposto il TTL, altrimenti SET potrebbe rimuoverlo o renderlo persistente
                    jedisCluster.expire(cacheKey, CACHE_TTL_SECONDS);
                }

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }



    /*
    public SmartFridge getRecipeById(String recipeId) {
        return null;
    } */

}