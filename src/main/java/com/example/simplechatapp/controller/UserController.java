package com.example.simplechatapp.controller;

import com.example.simplechatapp.dto.UserChatRoomDTO;
import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Log4j2
public class UserController {

    private final ChatRoomService chatRoomService;

    @GetMapping("/joined")
    public ResponseEntity<List<UserChatRoomDTO>> getUserChatRooms(@AuthenticationPrincipal UserDTO principal) {
        try {
            List<UserChatRoomDTO> chatRooms = chatRoomService.getUserChatRoom(principal.getEmail());
            return ResponseEntity.ok(chatRooms);
        } catch (Exception e) {
            log.error("Error while fetching user chat rooms", e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
