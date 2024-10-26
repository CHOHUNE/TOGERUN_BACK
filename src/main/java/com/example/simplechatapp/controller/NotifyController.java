package com.example.simplechatapp.controller;

import com.example.simplechatapp.dto.NotifyDto;
import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.service.NotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotifyController {

    private final NotifyService notifyService;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(
               @AuthenticationPrincipal UserDTO principal,
                                                @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {


        return ResponseEntity.ok(notifyService.subscribe(principal.getEmail(), lastEventId));
    }

    @GetMapping("/all")
    public ResponseEntity<NotifyDto.PageResponse> getAllNotifications(@AuthenticationPrincipal UserDTO principal,
                                                                        @RequestParam(value = "page", defaultValue = "0") int page,
                                                                       @RequestParam(value = "size", defaultValue = "5") int size){

        return ResponseEntity.ok(notifyService.getAllNotifications(principal.getEmail(), page, size));
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@AuthenticationPrincipal UserDTO principal, @PathVariable Long notificationId) {
        notifyService.markAsRead(principal.getEmail(), notificationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal UserDTO principal) {
        return ResponseEntity.ok(notifyService.getUnreadCount(principal.getEmail()));
    }

    @PostMapping("/clear")
    public ResponseEntity<Void> clearAll(@AuthenticationPrincipal UserDTO principal) {
        try {
            notifyService.markAsReadAll(principal.getEmail());
            return ResponseEntity.ok().build();
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }


    }
}
