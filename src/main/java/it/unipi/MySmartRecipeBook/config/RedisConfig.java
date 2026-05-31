package it.unipi.MySmartRecipeBook.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Redis Master - slace configuration
 */
@Configuration
public class RedisConfig {

    @Value("${app.redis.sentinel.nodes}")
    private String[] sentinelNodes;

    @Value("${app.redis.sentinel.master}")
    private String masterName;

    /**
     * Redis configuration for master slave
     * @return configured istance of JedisSentinelPool
     */
    @Bean
    public JedisSentinelPool jedisSentinelPool() {
        Set<String> sentinels = new HashSet<>(Arrays.asList(sentinelNodes));

        GenericObjectPoolConfig<Jedis> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(1);
        poolConfig.setJmxEnabled(false);

        return new JedisSentinelPool(masterName, sentinels, poolConfig, 2000);
    }
}