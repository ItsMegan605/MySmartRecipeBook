package it.unipi.MySmartRecipeBook.config;

import
        org.apache.commons.pool2.impl.GenericObjectPoolConfig;
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
import java.util.List;
import java.util.Set;

/**
 * Redis cluster configuration code
 */

@Configuration
public class RedisConfig {

    @Value("${app.redis.cluster.nodes}")
    private List<String> nodes;

    /**
     * Initializes and configures a JedisCluster.
     * Defines the initial set of cluster nodes with proper connections.
     * @return a configured JedisCluster  ready to execute commands across the nodes.
     *
     */
    @Bean
    public JedisCluster jedisCluster() {
        //defined cluster nodes
        Set<HostAndPort> clusterNodes = new HashSet<>();
        for (String node : nodes) {
            String[] parts = node.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            clusterNodes.add(new HostAndPort(host, port));
        }
        JedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
                .timeoutMillis(2000)
                .socketTimeoutMillis(2000)
                .build();

        GenericObjectPoolConfig<Connection> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(1);
        poolConfig.setJmxEnabled(false);

        return new JedisCluster(clusterNodes, clientConfig, 5, Duration.ofSeconds(2), poolConfig);
    }
}