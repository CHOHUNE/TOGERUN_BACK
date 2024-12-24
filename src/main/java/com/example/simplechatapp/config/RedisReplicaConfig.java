package com.example.simplechatapp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisReplicaConfig {


    @Value("${spring.data.redis.replica.host}")
    private String replicaHost;

    @Value("${spring.data.redis.replica.port}")
    private int replicaPort;

    @Value("${spring.data.redis.password}")
    private String password;

    @Bean(name = "replicaConnectionFactory")
    @Qualifier("replica")
    public RedisConnectionFactory replicaConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(replicaHost);
        config.setPort(replicaPort);
        config.setPassword(password);
        return new LettuceConnectionFactory(config);
    }

    @Bean(name = "replicaTemplate")
    @Qualifier("replica")
    public RedisTemplate<String, Object> replicaTemplate(
            @Qualifier("replicaConnectionFactory") RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        template.afterPropertiesSet();
        return template;
    }


    @Bean(name = "replicaCacheManager")
    @Qualifier("replica")
    public RedisCacheManager replicaCacheManager(
            @Qualifier("replicaConnectionFactory") RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withCacheConfiguration("member", defaultCacheConfig.entryTtl(Duration.ofMinutes(15)))
                .withCacheConfiguration("post", defaultCacheConfig.entryTtl(Duration.ofMinutes(15)))
                .withCacheConfiguration("chat", defaultCacheConfig.entryTtl(Duration.ofMinutes(15)))
                .withCacheConfiguration("view", defaultCacheConfig.entryTtl(Duration.ofMinutes(15)))
                .withCacheConfiguration("postComments", defaultCacheConfig.entryTtl(Duration.ofMinutes(15)))
                .build();
    }

}