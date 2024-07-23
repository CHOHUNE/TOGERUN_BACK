package com.example.simplechatapp.dto;

import com.example.simplechatapp.aop.proxy.NotifyInfo;
import com.example.simplechatapp.entity.ChatMessage;
import com.example.simplechatapp.entity.NotificationType;
import com.example.simplechatapp.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class ChatMessageDTO implements NotifyInfo {

    private Long id;
    private String content;
    private Long chatRoomId;
    private String nickname;
    private String email;
    private LocalDateTime createdAt;
    private String chatMessageType;

    private Set<User> receivers;
    private Long goUrlId;
    private NotificationType notificationType;




    public static ChatMessageDTO ChatMessageEntityToDto(ChatMessage chatMessage) {
        return
                ChatMessageDTO.builder()
                        .id(chatMessage.getId())
                        .content(chatMessage.getContent())
                        .chatRoomId(chatMessage.getChatRoom().getId())
                        .nickname(chatMessage.getUser().getNickname())
                        .email(chatMessage.getUser().getEmail())
                        .createdAt(chatMessage.getCreatedAt())
                        .chatMessageType(chatMessage.getChatMessageType().name())

                        .build();
    }

    @Override
    public Set<User> getReceiver() {
        return receivers;
    }

    @Override
    public Long getGoUrlId() {
        return goUrlId;
    }

    @Override
    public NotificationType getNotificationType() {
        return NotificationType.CHAT;
    }
}
