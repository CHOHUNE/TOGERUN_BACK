package com.example.simplechatapp.service;

import com.example.simplechatapp.dto.ChatMessageDTO;
import com.example.simplechatapp.entity.*;
import com.example.simplechatapp.repository.ChatMessageRepository;
import com.example.simplechatapp.repository.ChatRoomRepository;
import com.example.simplechatapp.repository.PostRepository;
import com.example.simplechatapp.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper; //Jackson 라이브러리를 사용하여 객체를 JSON으로 변환하거나 JSON을 객체로 변환하는데 사용


    //ReactiveRedisTemplate vs RedisTemplate  : ReactiveRedisTemplate 은 비동기 방식, RedisTemplate 은 동기 방식

//    @PostConstruct
//    public void setUp() {
//        objectMapper.registerModule(new JavaTimeModule()); //JavaTimeModule : Java 8의 날짜와 시간 API를 사용하여 시간을 직렬화하거나 역직렬화하는데 사용
//    }

    public List<ChatMessageDTO> getMessageByPostId(Long postId) {
        String redisKey = "chat:messages:" + postId;
        String cachedMessages = redisTemplate.opsForValue().get(redisKey);

        if (cachedMessages != null && !cachedMessages.isEmpty()) {
            try {
                List<ChatMessageDTO> messages = objectMapper.readValue(cachedMessages, new TypeReference<List<ChatMessageDTO>>() {});
                log.info("Cache Hit! {} messages", messages.size());
                return messages;
            } catch (Exception e) {
                log.error("Error deserializing messages from Redis {}", e.getMessage());
            }
        }

        List<ChatMessage> chatMessages = chatMessageRepository.findChatMessageByPostId(postId);
        List<ChatMessageDTO> messageDTOs = chatMessages.stream()
                .map(ChatMessageDTO::ChatMessageEntityToDto)
                .toList();

        if (!messageDTOs.isEmpty()) {
            try {
                String jsonMessages = objectMapper.writeValueAsString(messageDTOs);
                redisTemplate.opsForValue().set(redisKey, jsonMessages);
                redisTemplate.expire(redisKey, 1, TimeUnit.HOURS);
            } catch (Exception e) {
                log.error("Error serializing messages for Redis", e);
            }
        }

        return messageDTOs;
    }



    @Transactional
    public ChatRoom joinChatRoom(Long postId, String userEmail) throws JsonProcessingException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("POST NOT FOUND"));

        ChatRoom chatRoom = chatRoomRepository.findByPostId(postId)
                .orElseGet(() -> {
                    ChatRoom newChatRoom = new ChatRoom();
                    newChatRoom.setPost(post);
                    return chatRoomRepository.save(newChatRoom);
                });

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        if (!chatRoom.hasParticipant(user)) {
            chatRoom.addParticipant(user);

            ChatMessage joinMessage = ChatMessage.builder()
                    .chatRoom(chatRoom)
                    .content(user.getNickname() + "님이 입장하셨습니다.")
                    .user(user)
                    .createdAt(LocalDateTime.now())
                    .chatMessageType(ChatMessageType.SYSTEM)
                    .build();

            ChatMessage savedJoinMessage = chatMessageRepository.save(joinMessage);

            ChatMessageDTO joinMessageDTO = ChatMessageDTO.ChatMessageEntityToDto(savedJoinMessage);

            // Redis에 메시지 저장
            String redisKey = "chat:messages:" + postId;
            String cachedMessages = redisTemplate.opsForValue().get(redisKey);
            List<ChatMessageDTO> messages = cachedMessages != null ?
                    objectMapper.readValue(cachedMessages, new TypeReference<List<ChatMessageDTO>>() {}) :
                    new ArrayList<>();

            messages.add(joinMessageDTO);
            String updatedMessages = objectMapper.writeValueAsString(messages);
            redisTemplate.opsForValue().set(redisKey, updatedMessages);


            String jsonMessage = objectMapper.writeValueAsString(joinMessageDTO);
            // Redis Pub/Sub을 통해 메시지 발행
            redisTemplate.convertAndSend("chat." + postId, jsonMessage);

            // WebSocket을 통해 클라이언트에게 메시지 전송
            // 이 부분은 RedisSubscriber.java 에서 처리
//            messagingTemplate.convertAndSend("/topic/chat." + postId, joinMessageDTO);
        }

        return chatRoomRepository.save(chatRoom);
    }
}
