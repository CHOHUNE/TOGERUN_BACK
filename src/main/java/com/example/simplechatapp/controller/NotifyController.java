package com.example.simplechatapp.controller;

import com.example.simplechatapp.dto.NotifyDto;
import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.service.NotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotifyController {

    private final NotifyService notifyService;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(
//            @RequestParam("token") String token,
               @AuthenticationPrincipal UserDTO principal,
                                                @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {

//        Map<String, Object> claims = JWTUtil.validToken(token);
//        String email = (String) claims.get("email");

        return ResponseEntity.ok(notifyService.subscribe(principal.getEmail(), lastEventId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<NotifyDto.Response>> getAllNotifications(@AuthenticationPrincipal UserDTO principal,
                                                                        @RequestParam(value = "page", defaultValue = "0") int page,
                                                                       @RequestParam(value = "size", defaultValue = "7") int size){

        return ResponseEntity.ok(notifyService.getAllNotifications(principal.getEmail(),page,size));
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@AuthenticationPrincipal UserDTO principal, @PathVariable Long notificationId) {
        notifyService.markAsRead(principal.getEmail(), notificationId);
        return ResponseEntity.ok().build();
    }
}
