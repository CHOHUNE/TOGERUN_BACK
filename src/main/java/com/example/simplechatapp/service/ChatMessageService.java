package com.example.simplechatapp.service;

import com.example.simplechatapp.dto.ChatMessageDTO;
import com.example.simplechatapp.entity.ChatMessage;
import com.example.simplechatapp.entity.ChatRoom;
import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.repository.ChatMessageRepository;
import com.example.simplechatapp.repository.ChatRoomRepository;
import com.example.simplechatapp.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Builder
@Log4j2
public class ChatMessageService {


    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;


    @Transactional
    public ChatMessageDTO createChatMessage(ChatMessageDTO chatMessageRequestDTO, Long postId) throws JsonProcessingException {

        //RequestDTO 엔 email 과 content 만 있음

        User sender = userRepository.findByEmail(chatMessageRequestDTO.getEmail())
                .orElseThrow(()-> new RuntimeException("User Not Found"));

        ChatRoom chatRoom = chatRoomRepository.findByPostId(postId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found"));

        ChatMessage savedMessage = chatMessageRepository.save(
                ChatMessage.chatMessageDtoToEntity(chatMessageRequestDTO, chatRoom, sender));

        ChatMessageDTO responseDTO = ChatMessageDTO.ChatMessageEntityToDto(savedMessage);

        String redisKey = "chat:messages:" + postId;
        String cachedMessage = redisTemplate.opsForValue().get(redisKey);
        List<ChatMessageDTO> messages;

        if (cachedMessage != null) {
            messages = objectMapper.readValue(cachedMessage, new TypeReference<List<ChatMessageDTO>>() {});

        } else {
            messages = new ArrayList<>();
            }

        messages.add(responseDTO);

        String updatedMessages= objectMapper.writeValueAsString(messages);
        redisTemplate.opsForValue().set(redisKey, updatedMessages);
        redisTemplate.expire(redisKey, 1, TimeUnit.HOURS); // 1 시간 뒤 소멸


        String jsonMessage = objectMapper.writeValueAsString(responseDTO);
        redisTemplate.convertAndSend("chat." + postId, jsonMessage);

        // 이 부분은 RedisSubscriber.java 에서 처리
//        messagingTemplate.convertAndSend("/topic/chat." + postId, responseDTO);


        return responseDTO;

    }

}
