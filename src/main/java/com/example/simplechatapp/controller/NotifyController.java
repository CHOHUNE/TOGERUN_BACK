package com.example.simplechatapp.controller;

import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.service.NotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotifyController {

    private final NotifyService notifyService;

    // 클라이언트 구독 subscribe 메서드
    @GetMapping(value = "/subscribe/", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(@AuthenticationPrincipal User principal,
                                                @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {
        return ResponseEntity.ok(notifyService.subscribe(principal.getNickname(), lastEventId));
    }

    // 임시로 서버에서 클라이언트로 알림을 주기 위한 send-data 메서드
    @PostMapping("/send-data/{id}")
    public void sendData(@PathVariable Long id) {
        notifyService.notify(id, "data");
    }
}
