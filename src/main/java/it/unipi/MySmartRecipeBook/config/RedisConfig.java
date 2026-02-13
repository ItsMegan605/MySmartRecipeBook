package it.unipi.MySmartRecipeBook.config;
/*
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.*;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class RedisConfig {

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
}*/