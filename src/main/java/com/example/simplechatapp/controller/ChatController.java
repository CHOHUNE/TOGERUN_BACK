package com.example.simplechatapp.controller;

import com.example.simplechatapp.dto.ChatMessageDTO;
import com.example.simplechatapp.service.ChatMessageService;
import com.example.simplechatapp.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;


    @MessageMapping("/chat/{postId}/send")
    @SendTo("/topic/chat/{postId}")
    public Map<String,String> sendMessage(@Payload ChatMessageDTO requestDTO, @DestinationVariable Long postId) {


//        if(!chatRoomService.isUserAllowedInChatRoom(chatRoomId, email)){
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//        }

        chatMessageService.createChatMessage(requestDTO, postId);

        return Map.of("messageSending", "success");
    }
}
