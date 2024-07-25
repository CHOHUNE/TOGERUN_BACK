package com.example.simplechatapp.dto;


import com.example.simplechatapp.entity.Notify;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

        LocalDateTime createdAt;

        public static Response createResponse(Notify notify) {

            return Response.builder()
                    .id(notify.getId().toString())
                    .nickname(notify.getReceiver().getNickname())
                    .content(notify.getContent())
                    .type(notify.getNotificationType().name())
                    .createdAt(notify.getCreatedAt())
                    .build();
        }
    }
}
