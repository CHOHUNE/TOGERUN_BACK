package com.example.simplechatapp.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.lettuce.core.RedisException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisHighAvailabilityService {
    private static final int MAX_RETRY_ATTEMPTS = 3;

    @Value("${redis.retry.batch-size:100}")
    private int retryBatchSize;

    private final RedisTemplate<String, Object> masterTemplate;
    private final RedisTemplate<String, Object> replicaTemplate;
    private final SlackNotificationService slackNotification;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    private final AtomicBoolean isRecoveryRunning = new AtomicBoolean(false);
    private final AtomicBoolean masterAvailable = new AtomicBoolean(true);
    private final BlockingQueue<FailedOperation> failedOperationsQueue =
            new LinkedBlockingQueue<>(1000);

    private CircuitBreaker getCircuitBreaker() {
        return circuitBreakerRegistry.circuitBreaker("redis-breaker");
    }

    public Object read(String key) {
        CircuitBreaker circuitBreaker = getCircuitBreaker();

        try {
            return circuitBreaker.executeSupplier(() -> this.doRead(key));
        } catch (Exception e) {
            handleFailure("읽기 실패", e);
            return null;
        }
    }

    public void write(String key, Object value) {
        CircuitBreaker circuitBreaker = getCircuitBreaker();

        try {
            if (!masterAvailable.get()) {
                failedOperationsQueue.offer(new FailedOperation(key, value));
                throw new RuntimeException("Master 노드 사용 불가 - 작업 큐잉됨");
            }
            circuitBreaker.executeRunnable(() -> doWrite(key, value));
        } catch (Exception e) {
            handleFailure("쓰기 실패", e);
            masterAvailable.set(false);
            failedOperationsQueue.offer(new FailedOperation(key, value));
        }
    }

    @Scheduled(fixedRate = 5000)
    public void processFailedOperations() {
        if (!masterAvailable.get() ||
            !isRecoveryRunning.compareAndSet(false, true)) {
            return;
        }

        try {
            int processedCount = 0;

            while (!failedOperationsQueue.isEmpty() && processedCount < retryBatchSize) {
                FailedOperation operation = failedOperationsQueue.poll();
                if (operation != null) {
                    processOperation(operation, 0);
                    processedCount++;
                }
            }

            if (processedCount > 0) {
                log.info("Processed {} failed operations", processedCount);
                sendRecoveryAlert(String.format("재시도 작업 처리 완료 (%d건)", processedCount));
            }
        } finally {
            isRecoveryRunning.set(false);
        }
    }

    private void processOperation(FailedOperation operation, int attempt) {
        try {
            doWrite(operation.key, operation.value);
        } catch (Exception e) {
            if (attempt < MAX_RETRY_ATTEMPTS) {
                try {
                    Thread.sleep(calculateBackoffDelay(attempt));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                processOperation(operation, attempt + 1);
            } else {
                log.error("최대 재시도 횟수 초과: {}", operation);
            }
        }
    }

    private int calculateBackoffDelay(int attempt) {
        return Math.min(
                (int) Math.pow(2, attempt) * 1000,
                30_000  // 최대 30초
        );
    }

    private Object doRead(String key) {
        if (masterAvailable.get()) {
            try {
                return masterTemplate.opsForValue().get(key);
            } catch (Exception e) {
                log.error("Master 읽기 실패, Replica로 전환", e);
                masterAvailable.set(false);
                sendFailureAlert("Master 노드 읽기 실패");
                return readFromReplica(key);
            }
        }
        return readFromReplica(key);
    }

    private void doWrite(String key, Object value) {
        try {
            masterTemplate.opsForValue().set(key, value);
        } catch (RedisException e) {
            log.error("Redis 쓰기 작업 실패: {}", e.getMessage(), e);
            throw new RedisOperationException("쓰기 작업 중 오류 발생", e);
        }
    }

    private Object readFromReplica(String key) {
        try {
            log.info("Replica에서 읽기 시도");
            return replicaTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Replica 읽기 실패", e);
            sendFailureAlert("Replica 노드 읽기 실패");
            throw e;
        }
    }

    @Scheduled(fixedRateString = "30000")
    public void healthCheck() {
        try {
            boolean isMasterHealthy = checkHealth(masterTemplate);
            boolean isReplicaHealthy = checkHealth(replicaTemplate);

            if (isMasterHealthy && !masterAvailable.get()) {
                masterAvailable.set(true);
                sendRecoveryAlert("Master 노드 복구됨");
            } else if (!isMasterHealthy && masterAvailable.get()) {
                masterAvailable.set(false);
                sendFailureAlert("Master 노드 접속 불가");
            }

            if (!isReplicaHealthy) {
                sendFailureAlert("Replica 노드 접속 불가");
            }
        } catch (Exception e) {
            log.error("헬스 체크 실패", e);
            sendFailureAlert("Redis 헬스 체크 실패: " + e.getMessage());
        }
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

    private void handleFailure(String operation, Throwable throwable) {
        String message = String.format("""
            🚨 Redis 작업 실패
            • 작업: %s
            • 에러: %s
            • 발생시간: %s
            • Master 가용상태: %s
            """,
                operation,
                throwable.getMessage(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                masterAvailable.get() ? "사용가능" : "불가능"
        );

        slackNotification.sendAlert(message);
    }

    private void sendFailureAlert(String reason) {
        String message = String.format("""
            🚨 Redis 노드 장애
            • 사유: %s
            • 발생시간: %s
            • 현재상태: Master %s, Replica 사용 중
            """,
                reason,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                masterAvailable.get() ? "정상" : "비정상"
        );

        slackNotification.sendAlert(message);
    }

    private void sendRecoveryAlert(String message) {
        String alertMessage = String.format("""
            ✅ Redis 복구 알림
            • 내용: %s
            • 복구시간: %s
            """,
                message,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
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