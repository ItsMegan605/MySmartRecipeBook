package it.unipi.MySmartRecipeBook.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Connection;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisCluster;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * Redis cluster configuration code
 */

@Configuration
public class RedisConfig {

    /**
     *
     * @return the correct configuration, gets the host and ports and generates the cluster
     */

    @Bean
    public JedisCluster jedisCluster() {
        // Nodi del Cluster: per ora usiamo localhost su WSL
        Set<HostAndPort> clusterNodes = new HashSet<>();
        clusterNodes.add(new HostAndPort("127.0.0.1", 7004));
        clusterNodes.add(new HostAndPort("127.0.0.1", 7005));
        clusterNodes.add(new HostAndPort("127.0.0.1", 7006));

        JedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
                .timeoutMillis(2000)
                .socketTimeoutMillis(2000)
                .build();

        GenericObjectPoolConfig<Connection> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(1);
        poolConfig.setJmxEnabled(false); // Fondamentale per evitare l'UnableToRegisterMBeanException

        return new JedisCluster(clusterNodes, clientConfig, 5, Duration.ofSeconds(2), poolConfig);
    }
}