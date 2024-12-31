package com.example.simplechatapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackService {

    @Value("${slack.webhook.url}")
    private String webhookUrl;

    private final RestTemplate restTemplate;

    public void sendAlert(String message) {
        try {
            Map<String, String> slackMessage = Map.of(
                    "text", message
            );

            restTemplate.postForEntity(webhookUrl, slackMessage, String.class);
            log.info("Slack alert sent successfully");
        } catch (Exception e) {
            log.error("Failed to send Slack alert", e);
        }
    }
}