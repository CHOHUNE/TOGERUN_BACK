package com.example.simplechatapp.controller;

import com.example.simplechatapp.annotation.NeedNotify;
import com.example.simplechatapp.dto.ChatMessageDTO;
import com.example.simplechatapp.service.ChatMessageService;
import com.example.simplechatapp.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final ChatRoomService chatRoomService;


    @MessageMapping("/chat/{postId}/send")
    @SendTo("/topic/chat/{postId}")
    @NeedNotify
    public ChatMessageDTO sendMessage(@Payload ChatMessageDTO requestDTO, @DestinationVariable Long postId) {


//        if(!chatRoomService.isUserAllowedInChatRoom(chatRoomId, email)){
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//        }

        return chatMessageService.createChatMessage(requestDTO, postId);

//        return Map.of("messageSending", "success");
    }
}
