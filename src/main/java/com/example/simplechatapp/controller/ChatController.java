package com.example.simplechatapp.controller;

import com.example.simplechatapp.annotation.NeedNotify;
import com.example.simplechatapp.annotation.RateLimit;
import com.example.simplechatapp.dto.ChatMessageDTO;
import com.example.simplechatapp.service.ChatMessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j

public class ChatController {

    private final ChatMessageService chatMessageService;
    private final RedisTemplate<String,Object> redisTemplate;
    private final ObjectMapper objectMapper;


    @RateLimit(maxRequests = 20, duration = 10)
    @MessageMapping("/chat/{postId}/send")
    @SendTo("/topic/chat/{postId}")
    @NeedNotify
    public ChatMessageDTO sendMessage(@Payload ChatMessageDTO requestDTO, @DestinationVariable Long postId) throws JsonProcessingException {

        ChatMessageDTO savedMessage = chatMessageService.createChatMessage(requestDTO, postId);
        String jsonMessage = objectMapper.writeValueAsString(requestDTO);

        redisTemplate.convertAndSend("/topic/chat/"+postId,jsonMessage);

        return savedMessage;

    }
}
