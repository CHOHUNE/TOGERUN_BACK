package com.example.simplechatapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisHealthMonitorService {
    private final RedisConnectionFactory redisConnectionFactory;
    private final SlackService slackService;
    private AtomicBoolean masterHealthy = new AtomicBoolean(true);

    @Scheduled(fixedRateString = "${redis.health-check.interval:30000}")
    public void monitorRedisHealth() {
        boolean currentHealth = checkMasterHealth();
        boolean previousHealth = masterHealthy.get();

        if (previousHealth && !currentHealth) {
            // Master가 건강한 상태에서 장애 상태로 전환
            log.error("Redis Master node is down!");
            slackService.sendAlert("⚠️ Redis Master 노드 장애 발생\n" +
                                   "시간: " + LocalDateTime.now() + "\n" +
                                   "상태: 읽기 작업은 Replica에서 계속됩니다.");
            masterHealthy.set(false);
        } else if (!previousHealth && currentHealth) {
            // Master가 장애 상태에서 복구됨
            log.info("Redis Master node has recovered!");
            slackService.sendAlert("✅ Redis Master 노드 복구\n" +
                                   "시간: " + LocalDateTime.now() + "\n" +
                                   "상태: 정상 운영 중");
            masterHealthy.set(true);
        }
    }

    private boolean checkMasterHealth() {
        try {
            RedisConnection connection = redisConnectionFactory.getConnection();
            connection.ping();
            connection.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}