package com.example.simplechatapp.service;

import com.example.simplechatapp.dto.ChatMessageDTO;
import com.example.simplechatapp.entity.ChatMessage;
import com.example.simplechatapp.entity.ChatMessageType;
import com.example.simplechatapp.entity.ChatRoom;
import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.repository.ChatMessageRepository;
import com.example.simplechatapp.repository.ChatRoomRepository;
import com.example.simplechatapp.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Log4j2
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final RedisHealthMonitorService redisHealthMonitor;
    private final CircuitBreaker redisCircuitBreaker;

    @Transactional
    public ChatMessageDTO createChatMessage(ChatMessageDTO chatMessageRequestDTO, Long postId) throws JsonProcessingException {
        User sender = userRepository.findByEmail(chatMessageRequestDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        ChatRoom chatRoom = chatRoomRepository.findByPostId(postId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found"));

        ChatMessage savedMessage = chatMessageRepository.save(
                ChatMessage.chatMessageDtoToEntity(chatMessageRequestDTO, chatRoom, sender));

        ChatMessageDTO responseDTO = ChatMessageDTO.ChatMessageEntityToDto(savedMessage);

        // Redis 상태에 따른 메시지 전송
        if (redisHealthMonitor.isMasterHealthy()) {
            try {
                sendViaRedis(responseDTO, postId);
            } catch (Exception e) {
                log.error("Failed to send message via Redis", e);
                sendViaWebSocket(responseDTO, postId);
            }
        } else {
            sendViaWebSocket(responseDTO, postId);
        }

        return responseDTO;
    }

    private void sendViaRedis(ChatMessageDTO message, Long postId) throws JsonProcessingException {
        String jsonMessage = objectMapper.writeValueAsString(message);

        try {
            redisCircuitBreaker.decorateRunnable(() -> {
                redisTemplate.convertAndSend("chat." + postId, jsonMessage);
            }).run();
        } catch (Exception e) {
            log.error("Redis operation failed with circuit breaker", e);
            throw e;
        }
    }

    private void sendViaWebSocket(ChatMessageDTO message, Long postId) {
        String destination = "/topic/chat." + postId;
        messagingTemplate.convertAndSend(destination, message);
    }

    // 시스템 메시지 전송
    public void sendSystemMessage(Long postId, String content) {
        try {
            ChatMessageDTO systemMessage = ChatMessageDTO.builder()
                    .chatMessageType(String.valueOf(ChatMessageType.SYSTEM))
                    .content(content)
                    .createdAt(LocalDateTime.now())
                    .build();

            sendViaWebSocket(systemMessage, postId);
        } catch (Exception e) {
            log.error("Failed to send system message", e);
        }
    }
}