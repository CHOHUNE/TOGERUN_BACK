package com.example.simplechatapp.controller;

import com.example.simplechatapp.dto.FavoriteDTO;
import com.example.simplechatapp.dto.UserChatRoomDTO;
import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.dto.UserModifyDTO;
import com.example.simplechatapp.service.AuthenticationService;
import com.example.simplechatapp.service.ChatRoomService;
import com.example.simplechatapp.service.FavoriteService;
import com.example.simplechatapp.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Log4j2
public class UserController {

    private final ChatRoomService chatRoomService;
    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final FavoriteService favoriteService;


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

//        @GetMapping("/joinedNewVer")
//    public ResponseEntity<List<UserChatRoomDTO>> getUserChatRoomsNew(@AuthenticationPrincipal UserDTO principal) {
//        try {
//            List<UserChatRoomDTO> chatRooms = chatRoomService.getUserChatRoomNewVer(principal.getEmail());
//            return ResponseEntity.ok(chatRooms);
//        } catch (Exception e) {
//            log.error("Error while fetching user chat rooms", e);
//            return ResponseEntity.internalServerError().build();
//        }
//    }





    @GetMapping("/info")
    public UserDTO getUser(@AuthenticationPrincipal UserDTO principal) {
        UserDTO member = userService.getMember(principal.getEmail());

        log.info("getUserMemberInfo:{} ", member);
        return member;
    }

    @PutMapping("/modify")
    public ResponseEntity<?> modify(@RequestBody UserModifyDTO userModifyDTO,
                                    HttpServletResponse response,
                                    @AuthenticationPrincipal UserDTO currentUser) {
        log.info("Modifying user. UserModifyDTO: {}, CurrentUser: {}", userModifyDTO, currentUser);


        UserDTO updatedUser = userService.modifyMember(currentUser, userModifyDTO);

        // 새로운 토큰 생성 및 쿠키 설정
        authenticationService.setAuthenticationTokens(updatedUser, response);

        Map<String, Object> result = new HashMap<>();

        result.put("result", "modified");
        result.put("updatedUser", updatedUser);
        result.put("tokenRefreshed", true);

        return ResponseEntity.ok(result);
    }


    @GetMapping("/check/{nickname}")
    public ResponseEntity<?> checkNickNameAvailable(@PathVariable String nickname) {

        boolean isAvailable = userService.isNicknameAvailable(nickname);

        Map<String, Boolean> response = new HashMap<>();

        response.put("available", isAvailable);

        log.info("checkNickNameAvailable:{}", nickname);
        log.info("isAvailable:{}", isAvailable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/favorites")
    public List<FavoriteDTO> getFavoriteUser(@AuthenticationPrincipal UserDTO principal) {

        return favoriteService.getAllFavorites(principal.getEmail());
    }

}
