package com.example.simplechatapp.dto;

import com.example.simplechatapp.entity.ChatMessage;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatMessageDTO {

    private Long id;
    private String content;
    private Long chatRoomId;
    private String nickname;
    private String email;

    public static ChatMessageDTO ChatMessageEntityToDto(ChatMessage chatMessage) {
        return
                ChatMessageDTO.builder()
                        .id(chatMessage.getId())
                        .content(chatMessage.getContent())
                        .chatRoomId(chatMessage.getChatRoom().getId())
                        .nickname(chatMessage.getUser().getNickname())
                        .email(chatMessage.getUser().getEmail())
                        .build();
    }

}
