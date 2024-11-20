package com.example.simplechatapp.service;

import com.example.simplechatapp.dto.ChatMessageDTO;
import com.example.simplechatapp.dto.ChatRoomDTO;
import com.example.simplechatapp.dto.UserChatRoomDTO;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper; //Jackson 라이브러리를 사용하여 객체를 JSON으로 변환하거나 JSON을 객체로 변환하는데 사용


    public List<ChatMessageDTO> getMessageByPostId(Long postId) {
        String redisKey = "chat:messages:" + postId;
        String cachedMessages = redisTemplate.opsForValue().get(redisKey);

        if (cachedMessages != null && !cachedMessages.isEmpty()) {
            try {
                List<ChatMessageDTO> messages = objectMapper.readValue(cachedMessages, new TypeReference<List<ChatMessageDTO>>() {
                });
                log.info("Cache Hit! {} messages", messages.size());
                return messages;
            } catch (Exception e) {
                log.error("Error deserializing messages from Redis {}", e.getMessage());
            }
        }

        List<ChatMessage> chatMessages = chatMessageRepository.findChatMessageByPostId(postId);
        List<ChatMessageDTO> messageDTOs = chatMessages.stream().map(ChatMessageDTO::ChatMessageEntityToDto).toList();

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
    public ChatRoomDTO joinChatRoom(Long postId, String userEmail) throws JsonProcessingException {


        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("POST NOT FOUND"));
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User Not Found"));

        ChatRoom chatRoom = chatRoomRepository.findByPostId(postId).
                orElseGet(() -> {
                    ChatRoom newChatRoom = new ChatRoom();
                    newChatRoom.setPost(post);
                    newChatRoom.setActivityType(post.getActivityType());
                    newChatRoom.setParticipantCount(0); // 기본 0으로 세팅 -> 참여자가 추가될 때마다 증가
                    return chatRoomRepository.save(newChatRoom);
                });

        boolean isParticipant = chatRoom.hasParticipant(user);
        boolean isFull = chatRoom.getParticipantCount() >= post.getCapacity();
        boolean canJoin = !isFull || isParticipant;

        if (!canJoin) {
            throw new RuntimeException("채팅방에 참여하실 수 없습니다.");
        }

        if (canJoin && !isParticipant) {
            chatRoom.addParticipant(user);
            chatRoom.setParticipantCount(chatRoom.getParticipantCount() + 1); // 참여자 추가

            if (chatRoom.getParticipantCount() >= post.getCapacity()) {

                post.changeParticipateFlag(false);
                postRepository.save(post);
            }

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
            List<ChatMessageDTO> messages = cachedMessages != null ? objectMapper.readValue(cachedMessages, new TypeReference<List<ChatMessageDTO>>() {
            }) : new ArrayList<>();

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

        chatRoomRepository.save(chatRoom);

        return new ChatRoomDTO(chatRoom.getId(), post.getId(), chatRoom.getParticipants(), chatRoom.getParticipantCount(), isParticipant, canJoin);
    }

    @Transactional
    public void leaveChatRoom(Long postId, String userEmail) throws JsonProcessingException {

        ChatRoom chatRoom = chatRoomRepository.findByPostId(postId).orElseThrow(() -> new IllegalArgumentException("ChatRoom Not Found"));

        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User Not Found"));

        if (chatRoom.hasParticipant(user)) {
            chatRoom.removeParticipant(user);
            chatRoom.setParticipantCount(chatRoom.getParticipantCount() - 1); // 참여자 제거

            Post post = chatRoom.getPost();
            post.changeParticipateFlag(true);
            postRepository.save(post);

            ChatMessage leaveMessage = ChatMessage.builder().chatRoom(chatRoom).content(user.getNickname() + "님이 퇴장하셨습니다.").user(user).createdAt(LocalDateTime.now()).chatMessageType(ChatMessageType.SYSTEM).build();

            ChatMessage savedLeaveMessage = chatMessageRepository.save(leaveMessage);
            ChatMessageDTO leaveMessageDTO = ChatMessageDTO.ChatMessageEntityToDto(savedLeaveMessage);

            //Redis 에 메세지 저장

            String redisKey = "chat:messages:" + postId;
            String cachedMessages = redisTemplate.opsForValue().get(redisKey);

            List<ChatMessageDTO> messages = cachedMessages != null ? objectMapper.readValue(cachedMessages, new TypeReference<List<ChatMessageDTO>>() {
            }) : new ArrayList<>();

            messages.add(leaveMessageDTO);
            String updatedMessages = objectMapper.writeValueAsString(messages);
            redisTemplate.opsForValue().set(redisKey, updatedMessages);

            String jsonMessage = objectMapper.writeValueAsString(leaveMessageDTO);
            redisTemplate.convertAndSend("chat." + postId, jsonMessage);


        }
        chatRoomRepository.save(chatRoom);
    }


    @Transactional(readOnly = true) // 읽기 전용 트랜잭션 : 데이터베이스에서 데이터를 읽기만 하고 수정하지 않는 경우 사용
    public ChatRoomDTO getChatRoomStatus(Long postId, String email) {

        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("POST NOT FOUND"));
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User Not Found"));


        return chatRoomRepository.findByPostId(postId)
                .map(chatRoom -> {
                    boolean isParticipant = chatRoom.hasParticipant(user);
                    boolean isFull = chatRoom.getParticipantCount() >= post.getCapacity();
                    boolean canJoin = !isFull || isParticipant;
                    return new ChatRoomDTO(chatRoom.getId(), post.getId(), chatRoom.getParticipants(), chatRoom.getParticipantCount(), isParticipant, canJoin);
                }).orElseGet(() -> {
                    boolean canJoin = post.getCapacity() > 0;
                    return new ChatRoomDTO(null, post.getId(), new HashSet<>(), 0, false, canJoin);

                });
    }

    @Transactional(readOnly = true)
    public List<UserChatRoomDTO> getUserChatRoom(String userEmail) {
        return chatRoomRepository.findUserChatRoomDTOs(userEmail);
    }
    @Transactional(readOnly = true)
    public List<UserChatRoomDTO> getUserChatRoomNewVer(String userEmail) {
        return chatRoomRepository.findUserChatRoomDTOsNew(userEmail);
    }




    private UserChatRoomDTO convertTouserChatRoomDTO(ChatRoom chatRoom) {
        UserChatRoomDTO dto = new UserChatRoomDTO();

        dto.setChatRoomId(chatRoom.getId());
        dto.setPostId(chatRoom.getPost().getId());
        dto.setPostTitle(chatRoom.getPost().getTitle());
        dto.setParticipantCount(chatRoom.getParticipantCount());
        dto.setMeetingTime(chatRoom.getPost().getMeetingTime());
        dto.setCapacity(chatRoom.getPost().getCapacity());
        dto.setActivityType(chatRoom.getActivityType());

        // 마지막 메시지 정보 설정
        chatRoom.getChatMessageList().stream()
                .reduce((first, second) -> second) // 가장 최근 메시지 가져오기
                .ifPresent(lastMessage -> {
                    dto.setLastMessageTime(lastMessage.getCreatedAt());
                    dto.setLastMessagePreview(lastMessage.getContent().substring(0, Math.min(lastMessage.getContent().length(), 30)));
                });

        return dto;
    }}


