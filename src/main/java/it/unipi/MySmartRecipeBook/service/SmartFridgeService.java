package it.unipi.MySmartRecipeBook.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.MySmartRecipeBook.dto.IngredientsListDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.RecipeSuggestionDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.ShowRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.SliceRecipeDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.RecipeMongo;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Foodie;
import it.unipi.MySmartRecipeBook.repository.Mongo.FoodieRepository;
import it.unipi.MySmartRecipeBook.repository.Mongo.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.repository.Neo4j.RecipeNeo4jRepository;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import it.unipi.MySmartRecipeBook.utils.conversionFunctions.RecipeUtilityFunctions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import java.util.*;
import static it.unipi.MySmartRecipeBook.utils.parameters.Parameters.FILTERED_INGREDIENTS;

/**
 * Smart Fridge service that handles smart fridge's business logic operations
 */
@Service
public class SmartFridgeService {

    private final FoodieRepository foodieRepository;
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
                              ObjectMapper objectMapper, RecipeUtilityFunctions conversion, FoodieRepository foodieRepository){
        this.jedisSentinelPool = jedisSentinelPool;
        this.recipeRepository = recipeRepository;
        this.recipeNeo4jRepository = recipeNeo4jRepository;
        this.ingredientService = ingredientService;
        this.objectMapper = objectMapper;
        this.conversion = conversion;
        this.foodieRepository = foodieRepository;
    }


    private static final String REDIS_ENTITY = "Foodie:";
    private static final String REDIS_FRIDGE_PREFIX = ":smartFridge:ingredients";
    private static final String REDIS_RECIPES_PREFIX = ":smartFridge:suggestions";


    /**
     * Retrieves the smart fridge list of the ingredients of the authenticated foodie.
     * @return an {@link IngredientsListDTO} containing the set of ingredients that constitute the foodie's shopping list
     * @throws NoSuchElementException if the authenticated foodie is not found in the database
     */
    public IngredientsListDTO getSmartFridge() {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Foodie foodie = foodieRepository.findByUsername(authFoodie.getUsername())
                .orElseThrow(() -> new NoSuchElementException("Foodie not found!"));
        return returnSmartFridge(foodie.getUsername());
    }


    /**
     * Auxiliary method that builds the corresponding foodie's key and retrieves the smart fridge ingredients' list from Redis.
     * @param username the unique username of the target foodie
     * @return an {@link IngredientsListDTO} containing the set of ingredients that constitute the foodie's smart fridge
     */
    private IngredientsListDTO returnSmartFridge(String username) {

        String key = REDIS_ENTITY + username + REDIS_FRIDGE_PREFIX;

        Set<String> ingredients;
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            ingredients = jedis.smembers(key);
        }
        IngredientsListDTO ingredientsListDTO = new IngredientsListDTO();
        ingredientsListDTO.setIngredients(ingredients);

        return ingredientsListDTO;
    }


    /**
     * Adds a list of ingredients to the foodie's smart fridge ingredients' list. For each element of the list, we verify that it is
     * one of the allowed ingredients in the application: if not, the element is ignored, and we only add the valid ones.
     * Furthermore, the cached recipe suggestions are cleared to reflect the updated fridge content.
     * @param ingredients the list of ingredients to add
     * @return an {@link IngredientsListDTO} containing the foodie's smart fridge ingredients' list appropriately updated
     * @throws NoSuchElementException if the target foodie is not found in the database
     * @throws IllegalArgumentException if the list is null or empty, or if none of the inserted ingredients are valid
     */
    public IngredientsListDTO addIngredients(List<String> ingredients) {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Foodie foodie = foodieRepository.findByUsername(authFoodie.getUsername())
                .orElseThrow(() -> new NoSuchElementException("Foodie not found!"));

        if(ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException("No ingredients inserted");
        }

        ingredients.replaceAll(ingredient -> ingredient.strip().toLowerCase());
        ingredients.removeIf(ingredient -> !ingredientService.isValidIngredient(ingredient));

        String key = REDIS_ENTITY + foodie.getUsername() + REDIS_FRIDGE_PREFIX;

        if (!ingredients.isEmpty()) {
            try (Jedis jedis = jedisSentinelPool.getResource()) {
                jedis.sadd(key, ingredients.toArray(new String[0]));
                jedis.del(REDIS_ENTITY + foodie.getUsername() + REDIS_RECIPES_PREFIX);
                jedis.expire(key, 86400*15);

            }
        }
        else{
            throw new IllegalArgumentException("No valid ingredients inserted");
        }
        return returnSmartFridge(foodie.getUsername());
    }


    /**
     * Removes an ingredient from the foodie's smart fridge ingredients list. First, we verify that it is one of the allowed
     * ingredients in the application: if not, the element is ignored, otherwise it is removed from the list.
     * Furthermore, the {@code updateCacheAfterRemoval} method is invoked to correctly update the cached list of suggested recipes.
     * @param ingredient the name of the ingredient to remove
     * @return an {@link IngredientsListDTO} containing the smart fridge ingredients list appropriately updated
     * @throws NoSuchElementException if the target foodie is not found in the database
     * @throws IllegalArgumentException if the provided ingredient is null or blank
     */
    public IngredientsListDTO removeIngredient(String ingredient) {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Foodie foodie = foodieRepository.findByUsername(authFoodie.getUsername())
                .orElseThrow(() -> new NoSuchElementException("Foodie not found!"));

        if (ingredient == null || ingredient.isEmpty()) {
            throw new IllegalArgumentException("Invalid ingredient inserted");
        }

        ingredient = ingredient.strip().toLowerCase();

        if(ingredientService.isValidIngredient(ingredient)) {
            String key = REDIS_ENTITY + foodie.getUsername() + REDIS_FRIDGE_PREFIX;
            try (Jedis jedis = jedisSentinelPool.getResource()) {
                jedis.srem(key, ingredient);
                jedis.expire(key, 86400*15);
            }
            updateCacheAfterRemoval(foodie.getUsername(), ingredient);
        }
        return getSmartFridge();
    }


    /**
     * Updates the cached recipe recommendations after an ingredient is removed from the smart fridge.
     * The method checks if the removed ingredient is present in any of the cached recipes' matched ingredients.
     * If found, it removes the ingredient and decreases the match count. Recipes that fall below the minimum
     * threshold of 3 matching ingredients are discarded. The remaining recipes are then sorted in descending
     * order based on their match count and saved back to Redis. If no recipes remain valid, the cache is cleared.
     * @param username the unique identifier of the target foodie
     * @param removedIngredient the ingredient that has been removed from the smart fridge's list
     * @throws RuntimeException if an error occurs during the JSON serialization or deserialization of the cached data
     */
    private void updateCacheAfterRemoval(String username, String removedIngredient) {
        String cacheKey = REDIS_ENTITY + username + REDIS_RECIPES_PREFIX;
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
                        jedis.setex(cacheKey, 86400*15, objectMapper.writeValueAsString(updatedList));
                    }

                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Error occurred while using the application");
                }
            }
        }
    }


    /**
     * Retrieves a paginated list of recipe recommendations based on the ingredients available in the foodie's smart fridge.
     * @param username the unique username of the target foodie
     * @param pageNum the page number to retrieve
     * @return a {@link SliceRecipeDTO} containing a list of {@link RecipeSuggestionDTO} objects. Each suggestion includes
     * a recipe preview, the number of matched ingredients, and the specific fridge ingredients that matched,
     * along with two boolean values indicating the existence of previous or next pages.
     * @throws IllegalArgumentException if the page number is zero or negative, if the fridge contains fewer than
     * 3 usable ingredients, or if no matching recipes are found
     * @throws RuntimeException if an error occurs during the JSON serialization or deserialization of the cached data
     */
    public SliceRecipeDTO<RecipeSuggestionDTO> getRecommendations(String username, int pageNum) {

        if (pageNum <= 0) {
            throw new IllegalArgumentException("Invalid page number");
        }

        String cacheKey = REDIS_ENTITY + username + REDIS_RECIPES_PREFIX;

        String json;
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            json = jedis.get(cacheKey);
        }

        List<RecipeSuggestionDTO> suggestions;
        if (json != null) {
            try {
                suggestions = objectMapper.readValue(json, new TypeReference<>(){});
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error occurred while using the application");
            }
        }
        else {
            IngredientsListDTO ingredientsListDTO = getSmartFridge();
            Set<String> ingredientsSet = ingredientsListDTO.getIngredients();


            if (ingredientsSet == null || ingredientsSet.size() < 3) {
                throw new IllegalArgumentException("Insert at least 3 ingredients");
            }

            for (String ingredient : FILTERED_INGREDIENTS) {
                ingredientsSet.remove(ingredient);
            }

            if (ingredientsSet.size() < 3) {
                throw new IllegalArgumentException("Insert at least 3 valid ingredients");
            }

            List<String> ingredients = new ArrayList<>(ingredientsSet);
            suggestions = recipeNeo4jRepository.findRecipesByIngredients(ingredients);

            if (suggestions.isEmpty()) {
                throw new IllegalArgumentException("No recipe matching ingredients");
            }

            try (Jedis jedis = jedisSentinelPool.getResource()) {
                jedis.setex(cacheKey, 86400*15, objectMapper.writeValueAsString(suggestions));

            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error occurred while using the application");
            }
        }

        int start = (pageNum-1) * pageSize;
        int end = pageNum * pageSize;

        boolean hasPrevious = pageNum > 1;
        boolean hasNext = suggestions.size() > end;

        List<RecipeSuggestionDTO> content = new ArrayList<>();

        if(start < suggestions.size()) {
            int finalEnd = Math.min(suggestions.size(), end);
            content = suggestions.subList(start, finalEnd);
        }
        return new SliceRecipeDTO<>(content, hasNext, hasPrevious);
    }


    /**
     * Retrieves the details of a suggested recipe. If the requested recipe no longer exists
     * (e.g., deleted by the chef or due to the chef's profile deletion), the system automatically
     * removes it from the foodie's cached suggestions in Redis before throwing an exception.
     * @param id the unique identifier of the recipe to retrieve
     * @return a {@link ShowRecipeDTO} containing the detailed information of the requested recipe
     * @throws NoSuchElementException if the requested recipe or the authenticated foodie is not found
     * @throws RuntimeException if an error occurs during the JSON serialization or deserialization of the cached data
     */
    public ShowRecipeDTO getFridgeRecipeById(String id){

        Optional<RecipeMongo> fullRecipe = recipeRepository.findApprovedById(id);

        if(fullRecipe.isEmpty()){
            UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getPrincipal();

            Foodie foodie = foodieRepository.findByUsername(authFoodie.getUsername())
                    .orElseThrow(() -> new NoSuchElementException("Foodie not found!"));

            String fridgeKey = REDIS_ENTITY + foodie.getUsername() + REDIS_RECIPES_PREFIX;
            try (Jedis jedis = jedisSentinelPool.getResource()) {
                String suggestedRecipes = jedis.get(fridgeKey);
                if (suggestedRecipes != null) {
                    try {
                        List<RecipeSuggestionDTO> cachedRecipes = objectMapper.readValue(suggestedRecipes, new TypeReference<>() {
                        });
                        cachedRecipes.removeIf(recipe -> recipe.getId().equals(id));
                        jedis.setex(fridgeKey, 86400*15, objectMapper.writeValueAsString(cachedRecipes));

                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Error occurred while using the application");
                    }
                }
            }
            throw new NoSuchElementException("Recipe not found");
        }
        return conversion.entityToDto(fullRecipe.get());
    }

}
