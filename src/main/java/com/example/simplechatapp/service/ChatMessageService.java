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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Builder
@Log4j2
public class ChatMessageService {


    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;


    @Transactional
    public ChatMessageDTO createChatMessage(ChatMessageDTO chatMessageRequestDTO, Long postId){

        //RequestDTO 엔 email 과 content 만 있음

        User sender = userRepository.findByEmail(chatMessageRequestDTO.getEmail());

        ChatRoom chatRoom = chatRoomRepository.findByPostId(postId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found"));

        ChatMessage chatMessageResponseEntity = ChatMessage.chatMessageDtoToEntity(chatMessageRequestDTO, chatRoom,sender);

//        log.info("chatMessageRequestDTO{}",chatMessageRequestDTO.getEmail());
//        log.info("chatMessageRequestDTO{}",chatMessageRequestDTO.getContent());
//
//        log.info("sender{}",sender);

        chatMessageRepository.save(chatMessageResponseEntity);

        return ChatMessageDTO.ChatMessageEntityToDto(chatMessageResponseEntity);

//        return chatMessageRequestDTO;
    }
}
