package com.example.simplechatapp.service;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.RetryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Service
public class SlackNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(SlackNotificationService.class);

    private final RetryRegistry retryRegistry;
    private final RestTemplate restTemplate;
    private final Retry retry;

    @Value("${slack.webhook.url}")
    private String webhookUrl;

    public SlackNotificationService(RetryRegistry retryRegistry, RestTemplate restTemplate) {
        this.retryRegistry = retryRegistry;
        this.restTemplate = restTemplate;

        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(100))
                .retryExceptions(RuntimeException.class)
                .build();

        this.retry = retryRegistry.retry("slack-notification", retryConfig);
    }

    @PostConstruct
    public void init() {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            throw new IllegalStateException("Slack webhook URL is not configured");
        }
    }

    public void sendAlert(String message) {
        Supplier<Void> decoratedSupplier = Retry.decorateSupplier(retry, () -> {
            doSendAlert(message);
            return null;
        });

        try {
            decoratedSupplier.get();
        } catch (Exception e) {
            logger.error("Failed to send Slack notification after retries", e);
            throw new RuntimeException("Slack notification failed", e);
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
            logger.info("Slack notification sent successfully");
        } catch (Exception e) {
            logger.error("Error sending Slack notification", e);
            throw new RuntimeException("Network Error", e);
        }
    }
}