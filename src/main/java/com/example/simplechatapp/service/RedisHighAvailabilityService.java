package com.example.simplechatapp.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.lettuce.core.RedisConnectionException;
import io.lettuce.core.RedisException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class RedisHighAvailabilityService {
    private static final String CIRCUIT_BREAKER_NAME = "redis-master";

    private final RedisTemplate<String, Object> masterTemplate;
    private final RedisTemplate<String, Object> replicaTemplate;
    private final CircuitBreaker circuitBreaker;
    private final AtomicBoolean masterAvailable;
    private final long healthCheckInterval;

    public RedisHighAvailabilityService(
            RedisTemplate<String, Object> masterTemplate,
            RedisTemplate<String, Object> replicaTemplate,
            CircuitBreakerRegistry circuitBreakerRegistry,
            @Value("${redis.health-check.interval:30000}") long healthCheckInterval) {

        this.masterTemplate = masterTemplate;
        this.replicaTemplate = replicaTemplate;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_NAME);
        this.masterAvailable = new AtomicBoolean(true);
        this.healthCheckInterval = healthCheckInterval;

        log.info("Redis High Availability Service initialized with health check interval: {}ms", healthCheckInterval);
    }

    public Object read(String key) {
        try {
            if (masterAvailable.get()) {
                return circuitBreaker.executeSupplier(() -> readFromMaster(key));
            }
            return readFromReplica(key);
        } catch (Exception e) {
            log.error("Read operation failed for key: {}", key, e);
            return readFromReplica(key);
        }
    }

    public void write(String key, Object value) {
        try {
            if (!masterAvailable.get()) {
                throw new RedisOperationException("Master node is currently unavailable");
            }
            circuitBreaker.executeRunnable(() -> writeToMaster(key, value));
        } catch (Exception e) {
            log.error("Write operation failed for key: {}", key, e);
            masterAvailable.set(false);
            throw new RedisOperationException("Write operation failed", e);
        }
    }

    private Object readFromMaster(String key) {
        try {
            Object value = masterTemplate.opsForValue().get(key);
            log.debug("Successfully read from master: key={}", key);
            return value;
        } catch (RedisConnectionException | RedisConnectionFailureException e) {
            log.error("Master connection failed, switching to replica: key={}", key, e);
            masterAvailable.set(false);
            return readFromReplica(key);
        }
    }

    private Object readFromReplica(String key) {
        try {
            Object value = replicaTemplate.opsForValue().get(key);
            log.debug("Successfully read from replica: key={}", key);
            return value;
        } catch (RedisException e) {
            log.error("Replica read failed: key={}", key, e);
            throw new RedisOperationException("Replica read operation failed", e);
        }
    }

    private void writeToMaster(String key, Object value) {
        try {
            masterTemplate.opsForValue().set(key, value);
            log.debug("Successfully wrote to master: key={}", key);
        } catch (RedisException e) {
            log.error("Master write failed: key={}", key, e);
            throw new RedisOperationException("Master write operation failed", e);
        }
    }

    @Scheduled(fixedRateString = "${redis.health-check.interval:30000}")
    public void healthCheck() {
        boolean isMasterHealthy = checkNodeHealth(masterTemplate);
        boolean isReplicaHealthy = checkNodeHealth(replicaTemplate);

        handleMasterHealthStatus(isMasterHealthy);
        handleReplicaHealthStatus(isReplicaHealthy);

        log.debug("Health check completed - Master: {}, Replica: {}",
                isMasterHealthy ? "healthy" : "unhealthy",
                isReplicaHealthy ? "healthy" : "unhealthy");
    }

    private boolean checkNodeHealth(RedisTemplate<String, Object> template) {
        try {
            return Boolean.TRUE.equals(template.execute((RedisCallback<Boolean>) connection -> {
                try {
                    connection.ping();
                    return true;
                } catch (Exception e) {
                    log.warn("Failed to ping Redis node", e);
                    return false;
                }
            }));
        } catch (Exception e) {
            log.error("Failed to execute health check", e);
            return false;
        }
    }


    private void handleMasterHealthStatus(boolean isMasterHealthy) {
        if (isMasterHealthy && !masterAvailable.get()) {
            masterAvailable.set(true);
            log.info("Redis master node recovered and is now available");
        } else if (!isMasterHealthy && masterAvailable.get()) {
            masterAvailable.set(false);
            log.error("Redis master node is unavailable, switching to replica for read operations");
        }
    }

    private void handleReplicaHealthStatus(boolean isReplicaHealthy) {
        if (!isReplicaHealthy) {
            log.error("Redis replica node is unavailable");
        }
    }

    public static class RedisOperationException extends RuntimeException {
        public RedisOperationException(String message) {
            super(message);
        }

        public RedisOperationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}