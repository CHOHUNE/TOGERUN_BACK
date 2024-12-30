package com.example.simplechatapp.service;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackNotificationService {
    private static final String RETRY_NAME = "slack-notification-retry";
    private static final int MAX_ATTEMPTS = 3;
    private static final long WAIT_DURATION = 1000L;

    private final RetryRegistry retryRegistry;
    private final RestTemplate restTemplate;
    private Retry retry;

    @Value("${slack.alert.incident.webhook-url}")
    private String webhookUrl;

    @PostConstruct
    public void init() {
        if (StringUtils.isEmpty(webhookUrl)) {
            throw new IllegalStateException("Slack webhook URL must be configured in application.yml");
        }

        RetryConfig config = RetryConfig.custom()
                .maxAttempts(MAX_ATTEMPTS)
                .waitDuration(Duration.ofMillis(WAIT_DURATION))
                .retryExceptions(RuntimeException.class)
                .build();

        this.retry = retryRegistry.retry(RETRY_NAME, config);
    }

    public void sendAlert(String message) {
        Supplier<Void> decoratedSupplier = Retry.decorateSupplier(retry, () -> {
            doSendAlert(message);
            return null;
        });

        try {
            decoratedSupplier.get();
        } catch (Exception e) {
            log.error("Failed to send Slack notification after {} retries: {}", MAX_ATTEMPTS, e.getMessage(), e);
            throw new SlackNotificationException("Failed to send notification to Slack", e);
        }
    }

    private void doSendAlert(String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();
        payload.put("text", message);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            restTemplate.postForEntity(webhookUrl, request, String.class);
            log.debug("Slack notification sent successfully: {}", message);
        } catch (Exception e) {
            log.error("Error sending Slack notification: {}", e.getMessage());
            throw new SlackNotificationException("Network error while sending notification", e);
        }
    }

    public static class SlackNotificationException extends RuntimeException {
        public SlackNotificationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}