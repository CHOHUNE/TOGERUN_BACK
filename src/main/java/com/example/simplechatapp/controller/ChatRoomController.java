package com.example.simplechatapp.controller;

import com.example.simplechatapp.dto.ChatMessageDTO;
import com.example.simplechatapp.dto.ChatRoomDTO;
import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.service.ChatRoomService;
import com.example.simplechatapp.util.ChatRoomFullException;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post/{postId}")
@Log4j2
public class ChatRoomController {


    private final ChatRoomService chatRoomService;

    @PostMapping("/chat/join")
    public ResponseEntity<ChatRoomDTO> joinChatRoom(@PathVariable Long postId, @AuthenticationPrincipal UserDTO principal) throws JsonProcessingException {

        try {
            ChatRoomDTO chatRoomDTO = chatRoomService.joinChatRoom(postId, principal.getEmail());
            return ResponseEntity.ok(chatRoomDTO);
        } catch (ChatRoomFullException e) {
            // 채팅방이 가득 찼을 때의 처리
            // 여기서는 400 Bad Request를 반환하지만, 실제로는 클라이언트에 적절한 메시지를 전달해야 합니다.
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            // 기타 예외 처리
            return ResponseEntity.internalServerError().body(null);
        }

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

    @GetMapping("/chat/status")
    public ChatRoomDTO getChatRoomStatus(@PathVariable Long postId, @AuthenticationPrincipal UserDTO principal) {
        return chatRoomService.getChatRoomStatus(postId, principal.getEmail());

    }

    @ExceptionHandler(ChatRoomFullException.class)
    public RedirectView handleChatRoomFullException(ChatRoomFullException e, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("error", "채팅 방이 가득 찼습니다");
        return new RedirectView("/post");

    }
}
