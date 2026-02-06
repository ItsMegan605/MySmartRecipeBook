package it.unipi.MySmartRecipeBook.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.JedisPooled;

@Repository
public class SmartFridgeRepository {

    @Autowired
    private JedisPooled jedis; // Bean configurato nella tua RedisConfig

    private static final String PREFIX = "fridge:";

    public void save(String userId, String jsonFridge) {
        jedis.set(PREFIX + userId, jsonFridge);
    }

    public String findById(String userId) {
        return jedis.get(PREFIX + userId);
    }

    public void delete(String userId) {
        jedis.del(PREFIX + userId);
    }
}