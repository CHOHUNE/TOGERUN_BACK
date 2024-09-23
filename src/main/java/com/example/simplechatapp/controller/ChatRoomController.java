package com.example.simplechatapp.controller;

import com.example.simplechatapp.dto.ChatMessageDTO;
import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.entity.ChatRoom;
import com.example.simplechatapp.service.ChatRoomService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post/{postId}")
@Log4j2
public class ChatRoomController {


    private final ChatRoomService chatRoomService;

    @PostMapping("/chat/join")
    public ChatRoom joinChatRoom(@PathVariable Long postId,  @AuthenticationPrincipal UserDTO principal) throws JsonProcessingException {

        String email = principal.getEmail();
        return chatRoomService.joinChatRoom(postId, email);

    }

    @PostMapping("/chat/leave")
    public ResponseEntity<?> leaveChatRoom(@PathVariable Long postId, @AuthenticationPrincipal UserDTO principal) {
        try {
            chatRoomService.leaveChatRoom(postId, principal.getEmail());
            return ResponseEntity.ok().body("채팅방에서 퇴장하였습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.info("Error while leaving chat room", e);
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }
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
