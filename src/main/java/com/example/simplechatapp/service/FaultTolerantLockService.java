package com.example.simplechatapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class FaultTolerantLockService {
    private final RedisLockService redisLockService;
    private final RedisHealthMonitorService healthMonitorService;
    private final Map<String, ReentrantLock> localLocks = new ConcurrentHashMap<>();
    private static final String LOCAL_LOCK_PREFIX = "local:";

    public FaultTolerantLockService(RedisLockService redisLockService,
                                    RedisHealthMonitorService healthMonitorService) {
        this.redisLockService = redisLockService;
        this.healthMonitorService = healthMonitorService;
    }

    public boolean acquireLock(String key, long waitTimeSeconds) {
        String lockKey = getLockKey(key);

        // Redis 상태 확인
        if (!healthMonitorService.isMasterHealthy()) {
            log.warn("Redis master is unhealthy, falling back to local lock for key: {}", lockKey);
            return acquireLocalLock(lockKey);
        }

        try {
            // Redis 락 시도
            boolean acquired = redisLockService.acquireLock(key, waitTimeSeconds);
            if (acquired) {
                log.debug("Successfully acquired Redis lock for key: {}", lockKey);
                return true;
            }

            // Redis 락 획득 실패시 로컬 락으로 폴백
            log.warn("Failed to acquire Redis lock, falling back to local lock for key: {}", lockKey);
            return acquireLocalLock(lockKey);

        } catch (Exception e) {
            // Redis 에러 발생시 로컬 락으로 폴백
            log.error("Error occurred while acquiring Redis lock, falling back to local lock for key: {}. Error: {}",
                    lockKey, e.getMessage());
            return acquireLocalLock(lockKey);
        }
    }

    public void releaseLock(String key) {
        String lockKey = getLockKey(key);

        // Redis 상태 확인
        if (!healthMonitorService.isMasterHealthy()) {
            releaseLocalLock(lockKey);
            return;
        }

        try {
            redisLockService.releaseLock(key);
            log.debug("Successfully released Redis lock for key: {}", lockKey);
        } catch (Exception e) {
            log.error("Error occurred while releasing Redis lock for key: {}. Error: {}",
                    lockKey, e.getMessage());
            releaseLocalLock(lockKey);
        }
    }

    private boolean acquireLocalLock(String key) {
        ReentrantLock lock = localLocks.computeIfAbsent(key, k -> new ReentrantLock());
        try {
            boolean acquired = lock.tryLock();
            if (acquired) {
                log.debug("Successfully acquired local lock for key: {}", key);
                return true;
            }
            log.debug("Failed to acquire local lock for key: {}", key);
            return false;
        } catch (Exception e) {
            log.error("Error occurred while acquiring local lock for key: {}", key, e);
            return false;
        }
    }

    private void releaseLocalLock(String key) {
        ReentrantLock lock = localLocks.get(key);
        if (lock != null && lock.isHeldByCurrentThread()) {
            try {
                lock.unlock();
                localLocks.remove(key);
                log.debug("Successfully released local lock for key: {}", key);
            } catch (Exception e) {
                log.error("Error occurred while releasing local lock for key: {}", key, e);
            }
        }
    }

    private String getLockKey(String key) {
        return String.format("%s:%s", LOCAL_LOCK_PREFIX, key);

    }
}