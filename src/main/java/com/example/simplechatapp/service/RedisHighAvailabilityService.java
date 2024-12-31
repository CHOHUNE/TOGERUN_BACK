//package com.example.simplechatapp.service;
//
//import io.github.resilience4j.circuitbreaker.CircuitBreaker;
//import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
//import io.lettuce.core.RedisConnectionException;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.core.RedisCallback;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import java.util.concurrent.atomic.AtomicBoolean;
//
//@Slf4j
//@Service
//public class RedisHighAvailabilityService {
//    private static final String CIRCUIT_BREAKER_NAME = "redis";
//    private final RedisTemplate<String, Object> masterTemplate;
//    private final RedisTemplate<String, Object> replicaTemplate;
//    private final CircuitBreaker circuitBreaker;
//    private final AtomicBoolean masterAvailable;
//
//
//    public RedisHighAvailabilityService(
//            RedisTemplate<String, Object> masterTemplate,
//            RedisTemplate<String, Object> replicaTemplate,
//            CircuitBreakerRegistry circuitBreakerRegistry) {
//        this.masterTemplate = masterTemplate;
//        this.replicaTemplate = replicaTemplate;
//        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_NAME);
//        this.masterAvailable = new AtomicBoolean(true);
//    }
//
//    public Object readFromCache(String key) {
//        try {
//            if (masterAvailable.get()) {
//                return circuitBreaker.executeSupplier(() -> readFromMaster(key));
//            }
//            return readFromReplica(key);
//        } catch (Exception e) {
//            log.error("Redis read operation failed for key: {}", key, e);
//            throw new RuntimeException("Redis read operation failed", e);
//        }
//    }
//
//    public void writeToCache(String key, Object value) {
//        try {
//            if (masterAvailable.get()) {
//                masterTemplate.opsForValue().set(key, value);
//            } else {
//                throw new RuntimeException("Cannot write to cache - master node unavailable");
//            }
//        } catch (Exception e) {
//            log.error("Failed to write to cache for key: {}", key, e);
//            throw new RuntimeException("Cache write operation failed", e);
//        }
//    }
//
//    public void deleteFromCache(String key) {
//        try {
//            if (masterAvailable.get()) {
//                masterTemplate.delete(key);
//            } else {
//                throw new RuntimeException("Cannot delete from cache - master node unavailable");
//            }
//        } catch (Exception e) {
//            log.error("Failed to delete from cache for key: {}", key, e);
//            throw new RuntimeException("Cache delete operation failed", e);
//        }
//    }
//
//    private Object readFromMaster(String key) {
//        try {
//            return masterTemplate.opsForValue().get(key);
//        } catch (RedisConnectionException e) {
//            log.error("Master connection failed, switching to replica", e);
//            masterAvailable.set(false);
//            return readFromReplica(key);
//        }
//    }
//
//    private Object readFromReplica(String key) {
//        try {
//            return replicaTemplate.opsForValue().get(key);
//        } catch (Exception e) {
//            log.error("Replica read failed", e);
//            throw e;
//        }
//    }
//
//    @Scheduled(fixedRateString = "${redis.health-check.interval:30000}")
//    public void healthCheck() {
//        boolean isMasterHealthy = checkNodeHealth(masterTemplate);
//        if (isMasterHealthy && !masterAvailable.get()) {
//            log.info("Master node recovered, switching back to master");
//            masterAvailable.set(true);
//        }
//    }
//
//    private boolean checkNodeHealth(RedisTemplate<String, Object> template) {
//        try {
//            return Boolean.TRUE.equals(template.execute((RedisCallback<Boolean>) connection -> {
//                try {
//                    connection.ping();
//                    return true;
//                } catch (Exception e) {
//                    return false;
//                }
//            }));
//        } catch (Exception e) {
//            return false;
//        }
//    }
//}