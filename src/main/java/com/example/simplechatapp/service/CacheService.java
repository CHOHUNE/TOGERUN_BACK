package com.example.simplechatapp.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CacheService {
    private final Cache<String, Object> localCache;
    private final RedisTemplate<String, Object> masterTemplate;
    private final RedisTemplate<String, Object> replicaTemplate;
    private final CircuitBreaker circuitBreaker;

    public CacheService(
            Caffeine<Object, Object> caffeine,
            @Qualifier("redisTemplate") RedisTemplate<String, Object> masterTemplate,
            @Qualifier("replicaTemplate") RedisTemplate<String, Object> replicaTemplate,
            CircuitBreakerRegistry circuitBreakerRegistry) {
        this.localCache = caffeine.build();
        this.masterTemplate = masterTemplate;
        this.replicaTemplate = replicaTemplate;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("redisCircuitBreaker");
    }

    public Object get(String key, String cacheName) {
        // 1. Local Cache 조회
        Object localValue = localCache.getIfPresent(getCacheKey(cacheName, key));
        if (localValue != null) {
            return localValue;
        }

        // 2. Replica에서 조회 시도
        try {
            Object replicaValue = replicaTemplate.opsForValue().get(getCacheKey(cacheName, key));
            if (replicaValue != null) {
                localCache.put(getCacheKey(cacheName, key), replicaValue);
                return replicaValue;
            }
        } catch (Exception e) {
            log.warn("Replica read failed, falling back to master: {}", e.getMessage());
            // 3. Master에서 조회 시도
            return circuitBreaker.executeSupplier(() -> {
                try {
                    Object masterValue = masterTemplate.opsForValue().get(getCacheKey(cacheName, key));
                    if (masterValue != null) {
                        localCache.put(getCacheKey(cacheName, key), masterValue);
                    }
                    return masterValue;
                } catch (Exception ex) {
                    log.error("Master read failed: {}", ex.getMessage());
                    return null;
                }
            });
        }
        return null;
    }

    public void put(String key, Object value, String cacheName) {
        String cacheKey = getCacheKey(cacheName, key);

        // Master에 쓰기
        circuitBreaker.executeRunnable(() -> {
            try {
                masterTemplate.opsForValue().set(cacheKey, value);
                // Local Cache 업데이트
                localCache.put(cacheKey, value);
            } catch (Exception e) {
                log.error("Failed to write to master: {}", e.getMessage());
                // Master 실패시에도 Local Cache 업데이트
                localCache.put(cacheKey, value);
            }
        });
    }

    public void evict(String key, String cacheName) {
        String cacheKey = getCacheKey(cacheName, key);
        circuitBreaker.executeRunnable(() -> {
            try {
                masterTemplate.delete(cacheKey);
            } catch (Exception e) {
                log.error("Failed to evict from master: {}", e.getMessage());
            } finally {
                localCache.invalidate(cacheKey);
            }
        });
    }

    private String getCacheKey(String cacheName, String key) {
        return cacheName + ":" + key;
    }

    @Scheduled(fixedRate = 5000)
    @Async
    public void healthCheck() {
        // Redis 상태 체크
        boolean masterHealthy = checkRedisInstance(masterTemplate, "Master");
        boolean replicaHealthy = checkRedisInstance(replicaTemplate, "Replica");

        // Circuit Breaker 상태 체크
        CircuitBreaker.State state = circuitBreaker.getState();
        if (state != CircuitBreaker.State.CLOSED) {
            log.warn("Circuit Breaker is in {} state", state);

            if (!masterHealthy && !replicaHealthy) {
                log.error("Both Master and Replica are unhealthy!");
            }
        }
    }

    private boolean checkRedisInstance(RedisTemplate<String, Object> template, String instanceName) {
        try {
            template.execute((RedisCallback<?>) connection -> connection.ping());
            log.debug("{} Redis is healthy", instanceName);
            return true;
        } catch (Exception e) {
            log.error("{} Redis is unhealthy: {}", instanceName, e.getMessage());
            return false;
        }
    }

    @PostConstruct
    public void setupCircuitBreakerEventListener() {
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> {
                    log.info("Circuit Breaker state changed from {} to {}",
                            event.getStateTransition().getFromState(),
                            event.getStateTransition().getToState());
                })
                .onError(event -> {
                    log.error("Circuit Breaker error: {}", event.getThrowable().getMessage());
                });
    }
}