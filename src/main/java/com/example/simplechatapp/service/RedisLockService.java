package com.example.simplechatapp.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

@Service
@Slf4j
public class RedisLockService {
    private final RedisTemplate<String, String> redisTemplate;
    private final CircuitBreaker circuitBreaker;

    private static final String LOCK_KEY_PREFIX = "lock:";
    private static final String LOCK_VALUE = "LOCKED";
    private static final String CIRCUIT_BREAKER_NAME = "redisLock";

    public RedisLockService(RedisTemplate<String, String> redisTemplate,
                            CircuitBreakerRegistry circuitBreakerRegistry) {
        this.redisTemplate = redisTemplate;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_NAME);

        // CircuitBreaker 이벤트 리스너 등록
        this.circuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                        log.warn("Circuit Breaker State Change: {}", event));
    }

    public boolean acquireLock(String key, long waitTimeSeconds) {
        String lockKey = LOCK_KEY_PREFIX + key;

        Supplier<Boolean> lockOperation = () -> {
            try {
                return redisTemplate.opsForValue()
                        .setIfAbsent(lockKey, LOCK_VALUE, Duration.ofSeconds(waitTimeSeconds));
            } catch (Exception e) {
                log.error("Error acquiring Redis lock: {}", e.getMessage());
                throw e;
            }
        };

        try {
            return circuitBreaker.executeSupplier(lockOperation);
        } catch (Exception e) {
            log.warn("Circuit breaker prevented Redis lock operation: {}", e.getMessage());
            return false;
        }
    }

    public void releaseLock(String key) {
        String lockKey = LOCK_KEY_PREFIX + key;

        Runnable releaseOperation = () -> {
            try {
                Boolean deleted = redisTemplate.delete(lockKey);
                if (Boolean.FALSE.equals(deleted)) {
                    log.warn("Lock was not found for key: {}", lockKey);
                }
            } catch (Exception e) {
                log.error("Error releasing Redis lock: {}", e.getMessage());
                throw e;
            }
        };

        try {
            circuitBreaker.executeRunnable(releaseOperation);
        } catch (Exception e) {
            log.error("Circuit breaker prevented Redis lock release: {}", e.getMessage());
            throw new RuntimeException("Failed to release lock", e);
        }
    }
}