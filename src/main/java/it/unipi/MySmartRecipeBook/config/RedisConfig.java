package it.unipi.MySmartRecipeBook.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisCluster;


@Configuration
public class RedisConfig {

    /**
    serve a gestire nodi, connessioni, client etc
     mi sa che va fatto dopo aer definito le collezioni etc
     */
    @Bean
    public JedisCluster jedisCluster() {
        // 1. TODO: Definizione dei nodi del Cluster.
        // Inserisci qui gli IP reali del tuo cluster
        System.out.println("da fare");
        return null;

        // 2. Configurazione del Client (Timeout di connessione e socket)
        // Un timeout di 2 secondi è lo standard per evitare blocchi infiniti.

        // 3. Configurazione del Connection Pool
        // Gestisce quante connessioni simultanee l'app può aprire verso Redis.

        // 4. Creazione del Cluster
    }

}