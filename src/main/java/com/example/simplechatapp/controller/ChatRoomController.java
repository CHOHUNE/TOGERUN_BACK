package com.example.simplechatapp.controller;

import com.example.simplechatapp.entity.ChatRoom;
import com.example.simplechatapp.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatroom")
public class ChatRoomController {


    private final ChatRoomService chatRoomService;

    @PostMapping("/join")
    public ChatRoom joinChatRoom(@RequestParam Long postId, @RequestParam String email){

        return chatRoomService.joinChatRoom(postId, email);
    }

    @GetMapping
    public ResponseEntity<Map<String,Long>> getChatRoomId(
            @RequestParam Long postId,
            @RequestParam Long writerId,
            @RequestParam Long subscriberId){

        Long chatRoomId = chatRoomService.getChatRoomId(postId, writerId, subscriberId);
        Map<String, Long> response = new HashMap<>();

        response.put("chatRoomId", chatRoomId);

        return ResponseEntity.ok(response);
    }

}
