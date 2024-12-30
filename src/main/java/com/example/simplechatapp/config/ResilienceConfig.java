package com.example.simplechatapp.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.lettuce.core.RedisConnectionException;
import io.lettuce.core.RedisException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.RedisConnectionFailureException;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(10) // 설명 : 10개의 요청을 기반으로 에러 비율을 계산
                .failureRateThreshold(50.0f) // 설명 : 30% 이상의 에러 비율이 발생하면 CircuitBreaker가 열림
                .waitDurationInOpenState(Duration.ofSeconds(30)) // 설명 : CircuitBreaker가 열리면 20초 동안 CircuitBreaker가 열린 상태로 유지
                .permittedNumberOfCallsInHalfOpenState(5) // 설명 : CircuitBreaker가 열린 상태에서 5개의 요청을 허용
                .recordExceptions(
                        RedisConnectionException.class,
                        RedisConnectionFailureException.class,
                        RedisException.class
                )
                .build();

        return CircuitBreakerRegistry.of(config);
    }

    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(1))
                .build();

        return RetryRegistry.of(config);
    }
}