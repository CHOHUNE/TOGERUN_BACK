package com.example.simplechatapp.service;

import com.example.simplechatapp.dto.ChatMessageDTO;
import com.example.simplechatapp.entity.ChatMessage;
import com.example.simplechatapp.entity.ChatRoom;
import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.repository.ChatMessageRepository;
import com.example.simplechatapp.repository.ChatRoomRepository;
import com.example.simplechatapp.repository.UserRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Builder
@Log4j2
public class ChatMessageService {


    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final RedisTemplate<String, Object> redisTemplate;


    @Transactional
    public ChatMessageDTO createChatMessage(ChatMessageDTO chatMessageRequestDTO, Long postId){

        //RequestDTO 엔 email 과 content 만 있음

        User sender = userRepository.findByEmail(chatMessageRequestDTO.getEmail())
                .orElseThrow(()-> new RuntimeException("User Not Found"));

        ChatRoom chatRoom = chatRoomRepository.findByPostId(postId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found"));

        ChatMessage savedMessage = chatMessageRepository.save(
                ChatMessage.chatMessageDtoToEntity(chatMessageRequestDTO, chatRoom, sender));

        ChatMessageDTO responseDTO = ChatMessageDTO.ChatMessageEntityToDto(savedMessage);

        String redisKey = "chat:messages:" + postId;
        redisTemplate.opsForList().rightPush(redisKey,responseDTO);
        redisTemplate.expire(redisKey, 1, TimeUnit.HOURS); // 1 시간 뒤 소멸

        return responseDTO;

    }

}
