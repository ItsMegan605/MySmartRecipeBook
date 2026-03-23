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

@Order(1)
@Component
public class IngredientsPopulator implements CommandLineRunner {

    private JedisCluster jedisCluster;
    // key per ingredienti
    private static final String INGREDIENTS_REDIS_KEY = "MySmartRecipeBook:allowed_ingredients";
    @Value("${app.recipe.do-redis-population:false}")
    private boolean doRedisPopulation;

    public IngredientsPopulator(JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!doRedisPopulation) {
            return;
        }

        System.out.println("check redis state");

        try {
            // Controlla se la chiave esiste già
            if (jedisCluster.exists(INGREDIENTS_REDIS_KEY)) {
                System.out.println("Ingredients list already exists on redis");
                return;
            }

            System.out.println("Uploading the list");
            ClassPathResource resource = new ClassPathResource("ingredients.txt"); //trova il file nella cartella resources

            try (BufferedReader reader = new BufferedReader( //apre il file e leggo il testo a blocchi
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

                String ingredient;
                while ((ingredient = reader.readLine()) != null) { //leggo riga p'er riga fino alla fine
                    if (!ingredient.trim().isEmpty()) {
                        //sadd= comando redis per aggiungere a un Set
                        jedisCluster.sadd(INGREDIENTS_REDIS_KEY, ingredient.toLowerCase().trim());
                    }
                }
            }

            System.out.println("Ingredients updated successfully!");

        } catch (Exception e) { //mi dice il motivo
            System.out.println("WARNING! Can' communicate with redis: " + e.getMessage());
        }
    }
}
