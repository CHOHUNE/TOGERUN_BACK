package com.example.simplechatapp.dto;

import com.example.simplechatapp.aop.proxy.NotifyInfo;
import com.example.simplechatapp.entity.ChatMessage;
import com.example.simplechatapp.entity.NotificationType;
import com.example.simplechatapp.entity.NotifyMessage;
import com.example.simplechatapp.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO implements NotifyInfo {
//test
    private Long id;
    private String content;
    private Long chatRoomId;
    private String nickname;
    private String email;
    private LocalDateTime createdAt;
    private String chatMessageType;

    private Set<String> receivers;
    private String goUrlId;
    private NotificationType notificationType;
    private NotifyMessage notifyMessage;
    private Long postId;

    public static ChatMessageDTO ChatMessageEntityToDto(ChatMessage chatMessage) {
        String senderEmail = chatMessage.getUser().getEmail();

        return ChatMessageDTO.builder()
                .id(chatMessage.getId())
                .content(chatMessage.getContent())
                .chatRoomId(chatMessage.getChatRoom().getId())
                .nickname(chatMessage.getUser().getNickname())
                .email(senderEmail)
                .createdAt(chatMessage.getCreatedAt().atZone(ZoneId.of("UTC"))
                        .withZoneSameInstant(ZoneId.of("Asia/Seoul"))
                        .toLocalDateTime())
                .chatMessageType(chatMessage.getChatMessageType().name())
                .receivers(chatMessage.getChatRoom().getParticipants().stream()
                        .map(User::getEmail)
                        .filter(email -> !email.equals(senderEmail))
                        .collect(Collectors.toSet()))
                .goUrlId("/post/" + chatMessage.getChatRoom().getPost().getId() + "/chat/")
                .notificationType(NotificationType.CHAT)
                .notifyMessage(NotifyMessage.CHAT_APP_ALERT)
                .postId(chatMessage.getChatRoom().getPost().getId())
                .build();
    }

    @Override
    public Set<String> getReceiver() {
        return receivers;
    }

    @Override
    public String getGoUrlId() {
        return goUrlId;
    }

    @Override
    public NotificationType getNotificationType() {
        return NotificationType.CHAT;
    }

    @Override
    public NotifyMessage getNotifyMessage() {
        return NotifyMessage.CHAT_APP_ALERT;
    }

    @Override
    public Long getPostId() {
        return postId;
    }
}