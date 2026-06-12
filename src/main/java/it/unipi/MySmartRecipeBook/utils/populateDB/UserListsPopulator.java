package it.unipi.MySmartRecipeBook.utils.populateDB;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.MySmartRecipeBook.dto.recipe.RecipeSuggestionDTO;
import it.unipi.MySmartRecipeBook.repository.Neo4j.RecipeNeo4jRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Order(5)
@Component
public class UserListsPopulator implements CommandLineRunner {

    private final JedisSentinelPool jedisSentinelPool;
    private final ObjectMapper objectMapper;
    private final RecipeNeo4jRepository recipeNeo4jRepository;

    @Value("${app.recipe.do-list-population:false}")
    private boolean doRedisPopulation;

    private static final String REDIS_ENTITY = "Foodie:";
    private static final String REDIS_SHOPPING_LIST_PREFIX = ":shoppingList";
    private static final String REDIS_FRIDGE_PREFIX = ":smartFridge:ingredients";
    private static final String REDIS_RECIPES_PREFIX = ":smartFridge:suggestions";

    public UserListsPopulator(JedisSentinelPool jedisSentinelPool, ObjectMapper objectMapper, RecipeNeo4jRepository recipeNeo4jRepository) {
        this.jedisSentinelPool = jedisSentinelPool;
        this.objectMapper = objectMapper;
        this.recipeNeo4jRepository = recipeNeo4jRepository;
    }

    public static class UserListDTO {
        public String username;
        public List<String> shoppingList;
        public List<String> smartFridge;
    }

    private void getRecommendations(String username, Jedis jedis) {

        String fridgeKey = REDIS_ENTITY + username + REDIS_FRIDGE_PREFIX;
        Set<String> ingredientsSet = jedis.smembers(fridgeKey);

        if (ingredientsSet == null || ingredientsSet.size() < 3) {
            return;
        }

        List<String> ingredientsToSearchFor = new ArrayList<>(ingredientsSet);
        List<RecipeSuggestionDTO> suggestions = recipeNeo4jRepository.findRecipesByIngredients(ingredientsToSearchFor);

        if (suggestions == null || suggestions.isEmpty()) {
            return;
        }

        String cacheKey = REDIS_ENTITY + username + REDIS_RECIPES_PREFIX;
        try {
            jedis.setex(cacheKey, 86400 * 15, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(suggestions));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error occurred while saving recommendations to cache", e);
        }
    }

    @Override
    public void run(String... args) {
        if (!doRedisPopulation) {
            return;
        }

        System.out.println("Uploading User Lists to Redis...");
        ClassPathResource resource = new ClassPathResource("user_lists.json");

        if (!resource.exists()) {
            System.out.println("File user_lists.json non trovato.");
            return;
        }

        try (InputStream inputStream = resource.getInputStream();
             Jedis jedis = jedisSentinelPool.getResource()) { // CORREZIONE 2: Apriamo la connessione una sola volta qui

            List<UserListDTO> usersLists = objectMapper.readValue(inputStream, new TypeReference<>() {});

            for (UserListDTO user : usersLists) {
                String username = user.username;

                if (user.shoppingList != null && !user.shoppingList.isEmpty()) {
                    String shoppingKey = REDIS_ENTITY + username + REDIS_SHOPPING_LIST_PREFIX;
                    jedis.sadd(shoppingKey, user.shoppingList.toArray(new String[0]));
                    jedis.expire(shoppingKey, 86400 * 15);
                }

                if (user.smartFridge != null && !user.smartFridge.isEmpty()) {
                    String fridgeKey = REDIS_ENTITY + username + REDIS_FRIDGE_PREFIX;
                    jedis.sadd(fridgeKey, user.smartFridge.toArray(new String[0]));
                    jedis.expire(fridgeKey, 86400 * 15);
                }
                System.out.println("lists created, making suggestions ...");

                getRecommendations(username, jedis);
            }

            System.out.println("User lists and suggestions populated successfully in Redis!");

        } catch (Exception e) {
            System.out.println("WARNING! Can't communicate with redis or read JSON: " + e.getMessage());
        }
    }
}