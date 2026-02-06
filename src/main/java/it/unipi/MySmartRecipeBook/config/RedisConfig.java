package it.unipi.MySmartRecipeBook.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

@Configuration
public class RedisConfig {

    @Bean
    public JedisPooled jedisPooled() {
        // Usa i parametri definiti nel tuo application.properties
        // Host: localhost, Port: 6379
        return new JedisPooled("localhost", 6379);
    }
}

/*
Se il tuo obiettivo è un'architettura a cluster come l'altro progetto,
dovresti usare JedisCluster invece di JedisPooled, ma basandosi sul
tuo application.properties, attualmente punti a un'istanza singola locale.
 */