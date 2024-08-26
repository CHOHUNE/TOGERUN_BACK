package com.example.simplechatapp.service;


import com.example.simplechatapp.dto.ChatMessageDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class RedisSubscriber {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;


    public void onMessage(String message) {

        try {
            log.info("Received message: {}", message);

            ChatMessageDTO chatMessage = objectMapper.readValue(message, ChatMessageDTO.class);
            String destination = "/topic/chat." + chatMessage.getChatRoomId();


            messagingTemplate.convertAndSend(destination, chatMessage);
            log.info("Message sent to WebSocket: {}", chatMessage);

        } catch (Exception e ) {

            log.error("Error handling RedisMessage", e);

        }

    }
}
