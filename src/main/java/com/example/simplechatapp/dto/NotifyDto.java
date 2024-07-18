package com.example.simplechatapp.dto;


import com.example.simplechatapp.entity.Notify;
import jakarta.persistence.GeneratedValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class NotifyDto {

    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    @Data
    public static class Response{
        String id;
        String nickname;
        String content;
        String type;
        String createdAt;

        public static Response createResponse(Notify notify) {

            return Response.builder()
                    .id(notify.getId().toString())
                    .nickname(notify.getUser().getNickname())
                    .content(notify.getContent())
                    .type(notify.getNotificationType().name())
                    .createdAt(notify.getCreatedAt().toString())
                    .build();
        }
    }
}
