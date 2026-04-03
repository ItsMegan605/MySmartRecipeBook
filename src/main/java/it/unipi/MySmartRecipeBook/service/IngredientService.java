package it.unipi.MySmartRecipeBook.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCluster;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 *
 */
@Service
public class IngredientService {
    //metto cluster di redis visto che gli ingredienti andranno su redis anzichè la repo di mongo
    @Autowired
    private JedisCluster jedisCluster;
    // key per ingredienti
    private static final String INGREDIENTS_REDIS_KEY = "MySmartRecipeBook:allowed_ingredients";

    //controllo validità

    /**
     *
     * @param ingredientName
     * @return
     */
    public boolean isValidIngredient(String ingredientName) {
        if (ingredientName == null || ingredientName.trim().isEmpty()) {
            return false;
        }

        //sismember controlla se un membro appartiene a un certo set di chiavi
        return jedisCluster.sismember(INGREDIENTS_REDIS_KEY, ingredientName.toLowerCase().trim());
    }
}