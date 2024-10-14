package com.example.simplechatapp.dto;


import com.example.simplechatapp.entity.NotificationType;
import com.example.simplechatapp.entity.Notify;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class NotifyDto {

    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    @Data
    public static class Response{
        Long id;
        String nickname;
        String content;
        String type;
        String  goUrl;
        Boolean isRead;
        LocalDateTime createdAt;

        public Response(Long id, String nickname, String content,
                        NotificationType notificationType, String goUrl,
                        Boolean isRead, LocalDateTime createdAt) {
            this.id = id;
            this.nickname = nickname;
            this.content = content;
            this.type = notificationType.name();
            this.goUrl = goUrl;
            this.isRead = isRead;
            this.createdAt = createdAt;
        }

        public static Response createResponse(Notify notify) {

            return Response.builder()
                    .id(notify.getId())
                    .nickname(notify.getReceiver().getNickname())
                    .content(notify.getContent())
                    .type(notify.getNotificationType().name())
                    .goUrl(notify.getUrl())
                    .isRead(notify.getIsRead())
                    .createdAt(notify.getCreatedAt())
                    .build();
        }
    }

    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    @Data
    public static class PageResponse{
        List<Response> content;
        int totalPages;
        long totalElements;
        int currentPage;
//        int unreadCount;
    }
}
