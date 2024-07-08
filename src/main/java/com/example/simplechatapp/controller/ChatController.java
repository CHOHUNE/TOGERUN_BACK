package com.example.simplechatapp.controller;

import com.example.simplechatapp.dto.ChatMessageDTO;
import com.example.simplechatapp.entity.ChatRoom;
import com.example.simplechatapp.service.ChatMessageService;
import com.example.simplechatapp.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;



    @MessageMapping("/chat/{chatRoomId}/sendMessage")
    @SendTo("/topic/{chatRoomId}")
    public ResponseEntity<ChatMessageDTO> sendMessage(Principal principal, ChatMessageDTO requestDTO, @DestinationVariable Long chatRoomId) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = principal.getName();

        if(!chatRoomService.isUserAllowedInChatRoom(chatRoomId, email)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ChatMessageDTO responseDTO = chatMessageService.createChatMessage(requestDTO.getContent(), chatRoomId, email);

        return ResponseEntity.ok().body(responseDTO);
    }

}
