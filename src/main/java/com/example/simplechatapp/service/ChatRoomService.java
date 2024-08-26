package com.example.simplechatapp.service;

import com.example.simplechatapp.dto.ChatMessageDTO;
import com.example.simplechatapp.entity.*;
import com.example.simplechatapp.repository.ChatMessageRepository;
import com.example.simplechatapp.repository.ChatRoomRepository;
import com.example.simplechatapp.repository.PostRepository;
import com.example.simplechatapp.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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

    @PostConstruct
    public void setUp() {
        objectMapper.registerModule(new JavaTimeModule()); //JavaTimeModule : Java 8의 날짜와 시간 API를 사용하여 시간을 직렬화하거나 역직렬화하는데 사용
    }

    public List<ChatMessageDTO> getMessageByPostId(Long postId) {

        String redisKey = "chat:messages:" + postId;
        List<String> cachedMessages = redisTemplate.opsForList().range(redisKey, 0, -1);

        if (cachedMessages != null && !cachedMessages.isEmpty()) {

            log.info("Cache Hit!{} messages", cachedMessages.size());

            return cachedMessages.stream()
                    .map(this::desericalizableChatMessageDTO)
                    .collect(Collectors.toList());

        }


        List<ChatMessage> chatMessages = chatMessageRepository.findChatMessageByPostId(postId);
        List<ChatMessageDTO> messageDTOs = chatMessages.stream()
                .map(ChatMessageDTO::ChatMessageEntityToDto)
                .collect(Collectors.toList());

        messageDTOs.forEach(dto ->
                redisTemplate.opsForList().rightPush(redisKey, serializeChatMessageDTO(dto)));

        redisTemplate.expire(redisKey, 1, TimeUnit.HOURS); //1시간 후 만료되게 캐싱 설정

        return messageDTOs;
    }

    private String serializeChatMessageDTO(ChatMessageDTO dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (Exception e) {
            log.error("Error serializing ChatMessagedDTO", e);
            return null;
        }
    }

    private ChatMessageDTO desericalizableChatMessageDTO(String json) {

        try {
            return objectMapper.readValue(json, ChatMessageDTO.class);
        } catch (Exception e) {
            log.error("Error deserializing ChatMessageDTO", e);
            return null;

        }

    }

    @Transactional
    public ChatRoom joinChatRoom(Long postId, String userEmail) {

        // 클라이언트에서 버튼 클릭시 email 과 postId 를 전달 받고
        // 채팅방 유무 확인 후 생성 OR 참가
        // 기존 참가 여부 확인 후 참가 OR 불러오기

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new IllegalArgumentException(" POST NOT FOUND ")
        );
        // 포스트가 없으면 예외 처리


// 1번 채팅방 생성 유무 검증
        ChatRoom chatRoom = chatRoomRepository.findByPostId(postId)
                .orElseGet(() -> {
                    ChatRoom newChatRoom = new ChatRoom();
                    newChatRoom.setPost(post);
//                    newChatRoom.addParticipant(post.getUser()); // 게시글 작성자를 자동으로 추가 -> 입장 메세지가 안가서 주석 처리
                    return chatRoomRepository.save(newChatRoom);
                });
        //게시판 생성시 채팅방 생성은 비동기 처리 -> 채팅방에 등록된 포스트를 찾은 후 없으면 새로 생성

// 2번 참가자 추가 유무 검증

        User user = userRepository.findByEmail(userEmail).orElseThrow(()->new RuntimeException("User Not Found"));
        //해당 유저를 찾고

        if (user == null) {
            throw new IllegalArgumentException("USER NOT FOUND");
        }

        if (!chatRoom.hasParticipant(user)) {
            chatRoom.addParticipant(user);
            log.info("처음 참가한 사용자 입니다.");



            ChatMessage joinMessage = ChatMessage.builder()
                    .chatRoom(chatRoom)
                    .content(user.getNickname() + "님이 입장하셨습니다.")
                    .user(user)
                    .createdAt(LocalDateTime.now())
                    .chatMessageType(ChatMessageType.SYSTEM)
                    .build();

            chatMessageRepository.save(joinMessage);

            // Redis ---->

            String redisKey = "chat:messages:" + postId;

//            ChatMessageDTO joinMessageDTO = ChatMessageDTO.ChatMessageEntityToDto(joinMessage);
//            redisTemplate 변경에 따라 DTO 가 아닌 String 으로 전달

            redisTemplate.opsForList().rightPush(redisKey, String.valueOf(joinMessage));

            // Websocket 통해 모든 클라이언트에게 입장 메시지 전송
            messagingTemplate.convertAndSend("/topic/chat/" + postId, joinMessage);




        }

        return chatRoomRepository.save(chatRoom);

    }
}
