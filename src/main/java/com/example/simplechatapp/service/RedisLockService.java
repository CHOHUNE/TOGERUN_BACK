package com.example.simplechatapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisLockService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String LOCK_KEY_PREFIX = "lock:";
    private static final String LOCK_VALUE = "LOCKED";
    private static final long RETRY_INTERVAL = 100; // 100ms

    public boolean acquireLock(String key, long waitTimeSeconds) {
        String lockKey = LOCK_KEY_PREFIX + key;
        long waitTimeMillis = waitTimeSeconds * 1000;
        long startTime = System.currentTimeMillis();

        try {
            while (System.currentTimeMillis() - startTime < waitTimeMillis) {
                boolean acquired = redisTemplate.opsForValue()
                        .setIfAbsent(lockKey, LOCK_VALUE, Duration.ofSeconds(waitTimeSeconds));

                if (acquired) {
                    log.debug("Successfully acquired lock for key: {}", lockKey);
                    return true;
                }

                log.debug("Failed to acquire lock for key: {} (already locked), retrying...", lockKey);
                Thread.sleep(RETRY_INTERVAL);
            }

            log.debug("Failed to acquire lock for key: {} after {} seconds", lockKey, waitTimeSeconds);
            return false;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Lock acquisition interrupted for key: {}", lockKey);
            return false;
        } catch (Exception e) {
            log.warn("Error occurred while acquiring lock for key: {}. Error: {}", lockKey, e.getMessage());
            return false;
        }
    }

    public void releaseLock(String key) {
        String lockKey = LOCK_KEY_PREFIX + key;

        try {
            Boolean deleted = redisTemplate.delete(lockKey);
            if (Boolean.TRUE.equals(deleted)) {
                log.debug("Successfully released lock for key: {}", lockKey);
            } else {
                log.warn("Lock was not found for key: {}", lockKey);
                // 락이 이미 만료되었을 수 있으므로 예외를 던지지 않음
            }
        } catch (Exception e) {
            log.error("Error occurred while releasing lock for key: {}. Error: {}", lockKey, e.getMessage());
            throw new RuntimeException("Failed to release lock for key: " + lockKey, e);
        }
    }
}