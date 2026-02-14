/*package it.unipi.MySmartRecipeBook.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.JedisPooled;

//non so se serve intanto sistermo delle cose
@Repository
public class RecipeRedisRepository {

    @Autowired
    private JedisPooled jedis;

    public void saveRecipeThumbnail(String id, String jsonContent) {
        // Esempio: salva una stringa JSON con una scadenza (TTL)
        jedis.setex("recipe:" + id, 3600, jsonContent);
    }

    public String getRecipeThumbnail(String id) {
        return jedis.get("recipe:" + id);
    }
}
*/