package com.example.simplechatapp.config;


import com.example.simplechatapp.service.RedisRateLimitService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisRateLimiterConfig {

    @Bean
    public RedisRateLimitService redisRateLimitService(RedisTemplate<String, Object> redisTemplate) {

        return new RedisRateLimitService(redisTemplate);
    }
}
