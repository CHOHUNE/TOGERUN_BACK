package com.example.simplechatapp.config;


import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching // 캐시 사용을 위한 어노테이션
public class CacheConfig {
    @Bean
    public Caffeine caffeineConfig() {
        return Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES) // Redis TTL
                .initialCapacity(100) //
                .maximumSize(500);
    }

    @Bean
    public CacheManager caffeineCacheManager(Caffeine caffeine) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "member", "post", "chat", "view", "postComments"  // Redis와 동일한 캐시 이름
        );
        cacheManager.setCaffeine(caffeine);
        return cacheManager;
    }


}
