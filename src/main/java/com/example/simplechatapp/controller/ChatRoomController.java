package com.example.simplechatapp.controller;

import com.example.simplechatapp.dto.ChatMessageDTO;
import com.example.simplechatapp.entity.ChatRoom;
import com.example.simplechatapp.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatroom")
public class ChatRoomController {


    private final ChatRoomService chatRoomService;

    @PostMapping("/join")
    public ChatRoom joinChatRoom(@RequestParam Long postId, @RequestParam String email) {

        return chatRoomService.joinChatRoom(postId, email);
    }

    @GetMapping("/{chatRoomId}/messages")
    public List<ChatMessageDTO> getMessages(@PathVariable Long chatRoomId) {
        return chatRoomService.getMessageByChatRoomId(chatRoomId);
    }

//    @GetMapping("/{postId}") // post={postId} 가 아니다.
//    public ResponseEntity<Map<String,Long>> getChatRoomId(
//            @RequestParam Long postId,
//            @RequestParam Long writerId,
//            @RequestParam Long subscriberId){
//
//        Long chatRoomId = chatRoomService.getChatRoomId(postId, writerId, subscriberId);
//        Map<String, Long> response = new HashMap<>();
//
//        response.put("chatRoomId", chatRoomId);
//
//        return ResponseEntity.ok(response);
//    }

}
