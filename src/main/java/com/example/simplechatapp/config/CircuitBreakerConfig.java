package com.example.simplechatapp.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CircuitBreakerConfig {
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        // Circuit Breaker 설정
        io.github.resilience4j.circuitbreaker.CircuitBreakerConfig circuitBreakerConfig =
                io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                        .failureRateThreshold(50)        // 실패율 임계값
                        .waitDurationInOpenState(Duration.ofSeconds(10))  // Open 상태 지속 시간
                        .slidingWindowSize(10)           // 통계 수집 윈도우 크기
                        .minimumNumberOfCalls(5)         // 최소 호출 수
                        .build();

        return CircuitBreakerRegistry.of(circuitBreakerConfig);
    }
}