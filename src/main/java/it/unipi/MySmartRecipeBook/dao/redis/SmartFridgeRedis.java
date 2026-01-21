package it.unipi.MySmartRecipeBook.dao.redis;
import it.unipi.MySmartRecipeBook.dao.SmartFridgeDAO;
import it.unipi.MySmartRecipeBook.dao.base.BaseRedisDAO;
import it.unipi.MySmartRecipeBook.model.SmartFridge;
import com.google.gson.Gson;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


public class SmartFridgeRedis extends BaseRedisDAO implements SmartFridgeDAO {


    // Namespace: "smart-fridge" + "fridge" -> "smart-fridge:fridge:USER_ID"
    private static final String NAMESPACE = "smart-fridge";
    private static final String ENTITY = "fridge";

    private final Gson gson = new Gson();

    // Metodo helper per creare la chiave. Esempio: "smart-fridge:fridge:10:items"
    // Aggiungo ":items" per chiarezza, visto che useremo una LISTA Redis.
    private String getKey(Integer userId) {
        return NAMESPACE + ":" + ENTITY + ":" + userId + ":items";
    }



}


