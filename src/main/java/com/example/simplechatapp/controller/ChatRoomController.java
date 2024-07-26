package com.example.simplechatapp.controller;

import com.example.simplechatapp.dto.ChatMessageDTO;
import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.entity.ChatRoom;
import com.example.simplechatapp.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post/{postId}")
public class ChatRoomController {


    private final ChatRoomService chatRoomService;

    @PostMapping("/chat/join")
    public ChatRoom joinChatRoom(@PathVariable Long postId,  @AuthenticationPrincipal UserDTO principal) {

        String email = principal.getEmail();
        return chatRoomService.joinChatRoom(postId, email);
    }

    @GetMapping("/chat")
    public List<ChatMessageDTO> getMessages(@PathVariable Long postId) {
        return chatRoomService.getMessageByPostId(postId);
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
