package com.example.simplechatapp.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.lettuce.core.RedisConnectionException;
import io.lettuce.core.RedisException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class RedisHighAvailabilityService {
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int MAX_BACKOFF_DELAY = 30_000;
    private static final int QUEUE_CAPACITY = 1000;
    private static final String CIRCUIT_BREAKER_NAME = "redis-breaker";
    private static final String TIME_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final int QUEUE_OFFER_MAX_ATTEMPTS = 3;
    private static final long QUEUE_OFFER_BACKOFF = 100L;

    private static final String FAILURE_MESSAGE_TEMPLATE = """
        🚨 Redis 작업 실패
        • 작업: %s
        • 에러: %s
        • 발생시간: %s
        • Master 가용상태: %s
        """;

    private static final String ALERT_MESSAGE_TEMPLATE = """
        🚨 Redis 노드 장애
        • 사유: %s
        • 발생시간: %s
        • 현재상태: Master %s, Replica 사용 중
        """;

    private static final String RECOVERY_MESSAGE_TEMPLATE = """
        ✅ Redis 복구 알림
        • 내용: %s
        • 복구시간: %s
        """;

    private final RedisTemplate<String, Object> masterTemplate;
    private final RedisTemplate<String, Object> replicaTemplate;
    private final SlackNotificationService slackNotification;
    private final CircuitBreaker circuitBreaker;
    private final int retryBatchSize;
    private final long healthCheckInterval;

    private final AtomicBoolean isRecoveryRunning = new AtomicBoolean(false);
    private final AtomicBoolean masterAvailable = new AtomicBoolean(true);
    private final AtomicInteger failedQueueAttempts = new AtomicInteger(0);
    private final BlockingQueue<FailedOperation> failedOperationsQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);

    public RedisHighAvailabilityService(
            RedisTemplate<String, Object> masterTemplate,
            RedisTemplate<String, Object> replicaTemplate,
            SlackNotificationService slackNotification,
            CircuitBreakerRegistry circuitBreakerRegistry,
            @Value("${redis.retry.batch-size:100}") int retryBatchSize,
            @Value("${redis.health-check.interval:30000}") long healthCheckInterval) {

        this.masterTemplate = masterTemplate;
        this.replicaTemplate = replicaTemplate;
        this.slackNotification = slackNotification;
        this.retryBatchSize = retryBatchSize;
        this.healthCheckInterval = healthCheckInterval;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_NAME);

        log.info("Redis High Availability Service initialized with batch size: {}", retryBatchSize);
    }

    public Object read(String key) {
        try {
            return circuitBreaker.executeSupplier(() -> doRead(key));
        } catch (Exception e) {
            handleFailure("읽기 실패", e);
            return null;
        }
    }

    public void write(String key, Object value) {
        try {
            if (!masterAvailable.get()) {
                if (!queueOperation(key, value)) {
                    throw new RedisOperationException("큐 입력 실패 - 재시도 횟수 초과",
                            new IllegalStateException("Queue operation failed after " + QUEUE_OFFER_MAX_ATTEMPTS + " attempts"));
                }
                return;
            }
            circuitBreaker.executeRunnable(() -> doWrite(key, value));
        } catch (Exception e) {
            handleFailure("쓰기 실패", e);
            masterAvailable.set(false);
            if (!queueOperation(key, value)) {
                throw new RedisOperationException("큐 입력 실패 - 재시도 횟수 초과", e);
            }
        }
    }

    private boolean queueOperation(String key, Object value) {
        int attempts = 0;
        while (attempts < QUEUE_OFFER_MAX_ATTEMPTS) {
            try {
                if (failedOperationsQueue.offer(new FailedOperation(key, value))) {
                    failedQueueAttempts.set(0);
                    log.debug("Operation queued successfully: key={}, attempts={}", key, attempts + 1);
                    return true;
                }
                attempts++;
                Thread.sleep(QUEUE_OFFER_BACKOFF);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Queue operation interrupted while attempting to queue key: {}", key);
                return false;
            }
        }

        int totalFailures = failedQueueAttempts.incrementAndGet();
        if (totalFailures % 100 == 0) {  // 로그 폭주 방지
            log.error("Failed to queue operation after {} attempts. Total failures: {}",
                    QUEUE_OFFER_MAX_ATTEMPTS, totalFailures);
        }
        return false;
    }

    @Scheduled(fixedRateString = "${redis.retry.process-interval:5000}")
    public void processFailedOperations() {
        if (!masterAvailable.get()) {
            return;
        }

        if (!isRecoveryRunning.compareAndSet(false, true)) {
            log.debug("Recovery process is already running");
            return;
        }

        try {
            processOperationBatch();
        } catch (Exception e) {
            log.error("Failed to process operations batch: {}", e.getMessage(), e);
            slackNotification.sendAlert("Failed operation batch processing: " + e.getMessage());
        } finally {
            isRecoveryRunning.set(false);
        }
    }

    private void processOperationBatch() {
        int processedCount = 0;
        int failedCount = 0;
        int maxAttempts = Math.min(retryBatchSize, failedOperationsQueue.size());

        while (processedCount < maxAttempts) {
            FailedOperation operation = failedOperationsQueue.poll();
            if (operation == null) break;

            try {
                processOperation(operation, 0);
                processedCount++;
            } catch (Exception e) {
                failedCount++;
                if (!requeueFailedOperation(operation)) {
                    log.error("Failed to requeue operation: {}", operation, e);
                }
            }
        }

        if (processedCount > 0 || failedCount > 0) {
            log.info("Processed {} operations ({} failed)", processedCount, failedCount);
            sendRecoveryAlert(String.format("재시도 작업 처리 완료 (%d건 성공, %d건 실패)",
                    processedCount - failedCount, failedCount));
        }
    }

    private boolean requeueFailedOperation(FailedOperation operation) {
        try {
            return failedOperationsQueue.offer(operation);
        } catch (Exception e) {
            log.error("Error requeuing failed operation: {}", operation, e);
            return false;
        }
    }

    private void processOperation(FailedOperation operation, int attempt) {
        try {
            doWrite(operation.key, operation.value);
            log.debug("Successfully processed operation: key={}, attempt={}",
                    operation.key, attempt + 1);
        } catch (Exception e) {
            if (attempt < MAX_RETRY_ATTEMPTS) {
                handleRetry(operation, attempt, e);
            } else {
                handleMaxRetriesExceeded(operation, e);
            }
        }
    }

    private void handleRetry(FailedOperation operation, int attempt, Exception e) {
        try {
            long delay = calculateBackoffDelay(attempt);
            log.debug("Retrying operation after {}ms: key={}, attempt={}",
                    delay, operation.key, attempt + 1);
            Thread.sleep(delay);
            processOperation(operation, attempt + 1);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("Retry interrupted: key={}, attempt={}", operation.key, attempt + 1);
            requeueFailedOperation(operation);
        }
    }

    private void handleMaxRetriesExceeded(FailedOperation operation, Exception e) {
        log.error("Max retry attempts ({}) exceeded for operation: {}",
                MAX_RETRY_ATTEMPTS, operation, e);
        sendFailureAlert(String.format("최대 재시도 횟수 초과 - Key: %s", operation.key));
    }

    private int calculateBackoffDelay(int attempt) {
        return Math.min((int) Math.pow(2, attempt) * 1000, MAX_BACKOFF_DELAY);
    }

    private Object doRead(String key) {
        if (masterAvailable.get()) {
            try {
                Object value = masterTemplate.opsForValue().get(key);
                log.debug("Successfully read from master: key={}", key);
                return value;
            } catch (RedisConnectionException e) {
                log.error("Master connection failed for key: {}", key, e);
                masterAvailable.set(false);
                return readFromReplica(key);
            } catch (RedisException e) {
                log.error("Master read operation failed for key: {}", key, e);
                throw new RedisOperationException("Master 읽기 실패", e);
            }
        }
        return readFromReplica(key);
    }

    private Object readFromReplica(String key) {
        try {
            log.info("Attempting to read from replica: key={}", key);
            Object value = replicaTemplate.opsForValue().get(key);
            log.debug("Successfully read from replica: key={}", key);
            return value;
        } catch (RedisException e) {
            log.error("Replica read failed: key={}", key, e);
            throw new RedisOperationException("Replica 읽기 실패", e);
        }
    }

    private void doWrite(String key, Object value) {
        try {
            masterTemplate.opsForValue().set(key, value);
            log.debug("Successfully wrote to master: key={}", key);
        } catch (RedisException e) {
            log.error("Write operation failed: key={}", key, e);
            throw new RedisOperationException("쓰기 작업 중 오류 발생", e);
        }
    }

    @Scheduled(fixedRateString = "${redis.health-check.interval:30000}")
    public void healthCheck() {
        try {
            performHealthCheck();
        } catch (Exception e) {
            log.error("Health check failed: {}", e.getMessage(), e);
            sendFailureAlert("Redis 헬스 체크 실패: " + e.getMessage());
        }
    }

    private void performHealthCheck() {
        boolean isMasterHealthy = checkHealth(masterTemplate);
        boolean isReplicaHealthy = checkHealth(replicaTemplate);

        handleMasterHealthStatus(isMasterHealthy);
        handleReplicaHealthStatus(isReplicaHealthy);

        log.debug("Health check completed - Master: {}, Replica: {}",
                isMasterHealthy ? "healthy" : "unhealthy",
                isReplicaHealthy ? "healthy" : "unhealthy");
    }

    private boolean checkHealth(RedisTemplate<String, Object> template) {
        try {
            return Boolean.TRUE.equals(template.execute((RedisCallback<Boolean>) connection -> {
                try {
                    connection.ping();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }));
        } catch (Exception e) {
            return false;
        }
    }

    private synchronized void handleMasterHealthStatus(boolean isMasterHealthy) {
        if (isMasterHealthy && !masterAvailable.get()) {
            masterAvailable.set(true);
            sendRecoveryAlert("Master 노드 복구됨");
        } else if (!isMasterHealthy && masterAvailable.get()) {
            masterAvailable.set(false);
            sendFailureAlert("Master 노드 접속 불가");
        }
    }

    private void handleReplicaHealthStatus(boolean isReplicaHealthy) {
        if (!isReplicaHealthy) {
            sendFailureAlert("Replica 노드 접속 불가");
        }
    }

    private void handleFailure(String operation, Throwable throwable) {
        String message = String.format(FAILURE_MESSAGE_TEMPLATE,
                operation,
                throwable.getMessage(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(TIME_FORMAT_PATTERN)),
                masterAvailable.get() ? "사용가능" : "불가능"
        );

        slackNotification.sendAlert(message);
    }

    private void sendFailureAlert(String reason) {
        String message = String.format(ALERT_MESSAGE_TEMPLATE,
                reason,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(TIME_FORMAT_PATTERN)),
                masterAvailable.get() ? "정상" : "비정상"
        );

        slackNotification.sendAlert(message);
    }

    private void sendRecoveryAlert(String message) {
        String alertMessage = String.format(RECOVERY_MESSAGE_TEMPLATE,
                message,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(TIME_FORMAT_PATTERN))
        );

        slackNotification.sendAlert(alertMessage);
    }

    @Data
    @AllArgsConstructor
    private static class FailedOperation {
        private String key;
        private Object value;
    }

    public static class RedisOperationException extends RuntimeException {
        public RedisOperationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}