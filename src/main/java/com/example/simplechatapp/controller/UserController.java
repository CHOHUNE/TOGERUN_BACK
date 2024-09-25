package com.example.simplechatapp.controller;

import com.example.simplechatapp.dto.UserChatRoomDTO;
import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.dto.UserModifyDTO;
import com.example.simplechatapp.service.ChatRoomService;
import com.example.simplechatapp.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Log4j2
public class UserController {

    private final ChatRoomService chatRoomService;
    private final UserService userService;

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

    @GetMapping("/info")
    public UserDTO getUser(@AuthenticationPrincipal UserDTO principal) {
        UserDTO member = userService.getMember(principal.getEmail());

        log.info("getUserMemberInfo:{} " + member);
        return member;
    }

    @PutMapping("/modify")
    public Map<String, String> modify (@RequestBody UserModifyDTO userModifyDTO) {
        log.info("userModifyDTO{}", userModifyDTO);
        userService.modifyMember(userModifyDTO);

        return Map.of("result", "modified");

    }

}
