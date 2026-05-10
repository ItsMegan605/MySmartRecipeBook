package it.unipi.MySmartRecipeBook.utils.populateDB;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisCluster;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Populates the Redis cluster with the allowed ingredients list.
 */
@Order(1)
@Component
public class IngredientsPopulator implements CommandLineRunner {

    private JedisCluster jedisCluster;
    private static final String INGREDIENTS_REDIS_KEY = "MySmartRecipeBook:allowed_ingredients";
    @Value("${app.recipe.do-redis-population:false}")
    private boolean doRedisPopulation;

    public IngredientsPopulator(JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
    }

    /**
     * Executes the Redis ingredients population script on application startup if enabled in
     * application properties
     * @param args command line arguments
     * @throws Exception if an error occurs during file reading or Redis communication
     */
    @Override
    public void run(String... args) throws Exception {
        if (!doRedisPopulation) {
            return;
        }

        System.out.println("check redis state");

        try {
            if (jedisCluster.exists(INGREDIENTS_REDIS_KEY)) {
                System.out.println("Ingredients list already exists on redis");
                return;
            }

            System.out.println("Uploading the list");
            ClassPathResource resource = new ClassPathResource("ingredients.txt");

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

                String ingredient;
                while ((ingredient = reader.readLine()) != null) {
                    if (!ingredient.trim().isEmpty()) {
                        jedisCluster.sadd(INGREDIENTS_REDIS_KEY, ingredient.toLowerCase().trim());
                    }
                }
            }

            System.out.println("Ingredients updated successfully!");

        } catch (Exception e) {
            System.out.println("WARNING! Can't communicate with redis: " + e.getMessage());
        }
    }
}
