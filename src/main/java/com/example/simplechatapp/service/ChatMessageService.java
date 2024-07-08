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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Builder
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;


    @Transactional
    public ChatMessageDTO createChatMessage(String message, Long chatRoomId, String email){

        User sender = userRepository.findByEmail(email);

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found"));

        ChatMessage chatMessage = new ChatMessage(message, chatRoom, sender);

        ChatMessage saveChatMessage = chatMessageRepository.save(chatMessage);

        return ChatMessageDTO.toDto(saveChatMessage);

    }
}
