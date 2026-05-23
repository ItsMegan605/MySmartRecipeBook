package it.unipi.MySmartRecipeBook.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.MySmartRecipeBook.dto.IngredientsListDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.RecipeSuggestionDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.ShowRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.SliceRecipeDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.RecipeMongo;
import it.unipi.MySmartRecipeBook.repository.Mongo.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.repository.Neo4j.RecipeNeo4jRepository;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import it.unipi.MySmartRecipeBook.utils.conversionFunctions.RecipeUtilityFunctions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisSentinelPool;

// TODO: sostituire printStackTrace() con eccezione opportuna
import java.util.*;

import static it.unipi.MySmartRecipeBook.utils.parameters.Parameters.FILTERED_INGREDIENTS;

/**
 * Service managing Smart Fridge operations and business logic
 */
@Service
public class SmartFridgeService {

    @Value("${app.recipe.pag-size-chef:5}")
    private int pageSize;

    private final JedisSentinelPool jedisSentinelPool;
    private final RecipeMongoRepository recipeRepository;
    private final RecipeNeo4jRepository recipeNeo4jRepository;
    private final IngredientService ingredientService;
    private final ObjectMapper objectMapper;
    private final RecipeUtilityFunctions conversion;

    public SmartFridgeService(JedisSentinelPool jedisSentinelPool, RecipeMongoRepository recipeRepository,
                              RecipeNeo4jRepository recipeNeo4jRepository, IngredientService ingredientService,
                              ObjectMapper objectMapper, RecipeUtilityFunctions conversion){
        this.jedisSentinelPool = jedisSentinelPool;
        this.recipeRepository = recipeRepository;
        this.recipeNeo4jRepository = recipeNeo4jRepository;
        this.ingredientService = ingredientService;
        this.objectMapper = objectMapper;
        this.conversion = conversion;
    }

    public static final String REDIS_APP_NAMESPACE = "MySmartRecipeBook:";
    private static final String REDIS_FRIDGE_PREFIX = "smartFridge:ingredients:";
    private static final String REDIS_RECIPES_PREFIX = "smartFridge:suggestions:";

    /**
     * Method to get the smart fridge and its contents
     * @return The smart fridge of the specified user
     */
    public IngredientsListDTO getSmartFridge() {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return returnSmartFridge(authFoodie.getUsername());
    }

    /**
     * Method to return the smart fridge for the foodie
     * @param username - foodie's username
     * @return the ingredients list
     */
    private IngredientsListDTO returnSmartFridge(String username) {

        String key = REDIS_APP_NAMESPACE + REDIS_FRIDGE_PREFIX + username;

        Set<String> ingredients;
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            ingredients = jedis.smembers(key);
        }
        IngredientsListDTO ingredientsListDTO = new IngredientsListDTO();
        ingredientsListDTO.setIngredients(ingredients);

        return ingredientsListDTO;
    }

    /**
     * Method to add ingredients to the Smart fridge
     * @param ingredients - list of ingredients
     * @return the new Fridge with the added ingredients
     */
    public IngredientsListDTO addIngredients(List<String> ingredients) {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if(ingredients == null) {
            throw new IllegalArgumentException("No ingredients inserted");
        }

        ingredients.replaceAll(ingredient -> ingredient.strip().toLowerCase());
        ingredients.removeIf(ingredient -> !ingredientService.isValidIngredient(ingredient));

        String key = REDIS_APP_NAMESPACE + REDIS_FRIDGE_PREFIX + authFoodie.getUsername();

        if (!ingredients.isEmpty()) {
            try (Jedis jedis = jedisSentinelPool.getResource()) {
                jedis.sadd(key, ingredients.toArray(new String[0]));
                jedis.del(REDIS_APP_NAMESPACE +REDIS_RECIPES_PREFIX + authFoodie.getUsername());
            }
        }
        else{
            throw new IllegalArgumentException("No valid ingredients inserted");
        }
        return returnSmartFridge(authFoodie.getUsername());
    }


    /**
     * Method to remove ingredients from the Smart Fridge
     * @param ingredient - ingredient to delete
     * @return the new Smart fridge
     */
    public IngredientsListDTO removeIngredient(String ingredient) {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        ingredient = ingredient.strip().toLowerCase();

        if(ingredientService.isValidIngredient(ingredient)) {
            String key = REDIS_APP_NAMESPACE + REDIS_FRIDGE_PREFIX + authFoodie.getUsername();
            try (Jedis jedis = jedisSentinelPool.getResource()) {
                jedis.srem(key, ingredient);
            }
            updateCacheAfterRemoval(authFoodie.getUsername(), ingredient);
        }

        return getSmartFridge();
    }

    /**
     * Method to get recommendations given the ingredients in the fridge
     * @param username - foodie's username
     * @return the recipes suggested
     */
    public SliceRecipeDTO<RecipeSuggestionDTO> getRecommendations(String username, int pageNum) {
        String cacheKey = REDIS_APP_NAMESPACE +REDIS_RECIPES_PREFIX + username;
        String json;
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            json = jedis.get(cacheKey);
        }

        if (json != null) {
            try {
                return objectMapper.readValue(json, new TypeReference<>(){});
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        //If the recipe is not cached, check if there are at least 3 matched ingredients
        IngredientsListDTO ingredientsListDTO = getSmartFridge();
        Set<String> ingredientsSet = ingredientsListDTO.getIngredients();

        for(String ingredient : FILTERED_INGREDIENTS){
           ingredientsSet.remove(ingredient);
        }

        if (ingredientsSet == null || ingredientsSet.size() < 3) {
            throw new IllegalArgumentException("Insert at least 3 ingredients");
        }

        List<String> ingredients = new ArrayList<>(ingredientsSet);
        List<RecipeSuggestionDTO> suggestions = recipeNeo4jRepository.findRecipesByIngredients(ingredients);

        if(suggestions.isEmpty()){
            throw new IllegalArgumentException("No recipe matching ingredients");
        }


        try {
            try (Jedis jedis = jedisSentinelPool.getResource()) {
                jedis.set(cacheKey, objectMapper.writeValueAsString(suggestions));
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        int start = (pageNum-1)*pageSize;
        int end = pageNum*pageSize;

        boolean hasPrevious = pageNum != 1;
        boolean hasNext = suggestions.size() > end;

        List<RecipeSuggestionDTO> content = suggestions.subList(start, end);

        return new SliceRecipeDTO<>(content, hasNext, hasPrevious);
    }

    /**
     * Method to update the user's cache after an ingredient has been removed
     * @param username - foodie's username
     * @param removedIngredient - the removed ingredient
     */
    private void updateCacheAfterRemoval(String username, String removedIngredient) {
        String cacheKey = REDIS_APP_NAMESPACE +REDIS_RECIPES_PREFIX + username;
        try (Jedis jedis = jedisSentinelPool.getResource()){
            String json = jedis.get(cacheKey);

            if (json != null) {
                try {
                    List<RecipeSuggestionDTO> cachedRecipes = objectMapper.readValue(json, new TypeReference<>(){});
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

                        if (recipe.getMatchedIngredients().size() >= 3) {
                            updatedList.add(recipe);
                        }
                    }

                    if (updatedList.isEmpty()) {
                        jedis.del(cacheKey);
                    } else {
                        updatedList.sort(Comparator.comparingInt(RecipeSuggestionDTO::getMatchCount).reversed());
                        jedis.set(cacheKey, objectMapper.writeValueAsString(updatedList));
                    }

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Method to get the recipe once we click on it
     * @param id - id of the recipe
     * @return the recipe
     */
    public ShowRecipeDTO getFridgeRecipeById(String id){

        Optional<RecipeMongo> full_recipe = recipeRepository.findById(id);

        if(full_recipe.isEmpty()){
            UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getPrincipal();

            String fridgeKey = "MySmartRecipeBook:smartFridge:suggestions:" + authFoodie.getUsername();
            try (Jedis jedis = jedisSentinelPool.getResource()) {
                String suggestedRecipes = jedis.get(fridgeKey);
                if (suggestedRecipes != null) {
                    try {
                        List<RecipeSuggestionDTO> cachedRecipes = objectMapper.readValue(suggestedRecipes, new TypeReference<>() {
                        });
                        cachedRecipes.removeIf(recipe -> recipe.getId().equals(id));

                        jedis.set(fridgeKey, objectMapper.writeValueAsString(cachedRecipes));

                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
            }
            throw new NoSuchElementException("Recipe not found");
        }
        return conversion.EntityToDto(full_recipe.get());
    }

}
