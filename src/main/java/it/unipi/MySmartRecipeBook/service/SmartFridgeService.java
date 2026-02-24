package it.unipi.MySmartRecipeBook.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.MySmartRecipeBook.dto.recipe.RecipeSuggestionDTO;
import it.unipi.MySmartRecipeBook.model.Redis.SmartFridge;
import it.unipi.MySmartRecipeBook.repository.FoodieRepository;
import it.unipi.MySmartRecipeBook.repository.RecipeNeo4jRepository;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCluster;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
    private static final String REDIS_FRIDGE_PREFIX = "smartFridge:items:";
    private static final String REDIS_RECIPES_PREFIX = "smartFridge:suggestions:";



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
        if (!ingredientService.isValidIngredient(ingredient)) {
            throw new IllegalArgumentException("The ingredient: " + ingredient + " is not allowed!");
        }
        if (!foodieRepository.existsById(username)) {
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

        String json = jedisCluster.get(cacheKey);
        if (json != null) {
            try {
                return objectMapper.readValue(json, new TypeReference<List<RecipeSuggestionDTO>>(){});
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        //if the recipe is not already in the cache check if there are the matched 3 ingredients
        SmartFridge fridge = getSmartFridge(username);
        List<String> ingredients = fridge.getIngredients();

        if (ingredients.size() < 3) {
            return new ArrayList<>();
        }

        //and we check in neo4j
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

    public SmartFridge removeItem(String username, String ingredient) {
        if (!foodieRepository.existsById(username)) {
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
