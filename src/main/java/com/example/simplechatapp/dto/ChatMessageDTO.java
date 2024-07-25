package com.example.simplechatapp.dto;

import com.example.simplechatapp.aop.proxy.NotifyInfo;
import com.example.simplechatapp.entity.ChatMessage;
import com.example.simplechatapp.entity.NotificationType;
import com.example.simplechatapp.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

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

    private Set<String> receivers; // User 를 직접 참조 했다가 오류가 생김 - > UserDTO 참조로 변경 -> String 으로 변경
    //  Hibernate 는 지연로딩을 위해 프록시 객체를 사용하는데 이 Jackson 라이브러리는 프록시 객체를 처리하지 못한다.
    // Jackson 라이브러리란 ? : 자바 객체를 JSON으로 변환하거나 JSON을 자바 객체로 변환하는데 사용하는 라이브러리
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
                        .receivers(chatMessage.getChatRoom().getParticipants().parallelStream()
                                .map(User::getEmail)
                                .collect(Collectors.toSet()))
                        .goUrlId(chatMessage.getChatRoom().getId())
                        .notificationType(NotificationType.CHAT)



                        .build();
    }

    @Override
    public Set<String> getReceiver() {
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
