package com.example.simplechatapp.dto;

import com.example.simplechatapp.entity.ChatMessage;
import lombok.Data;

@Data
public class ChatMessageDTO {

    private Long id;
    private String content;
    private Long chatRoomId;
    private String nickname;

    public static ChatMessageDTO toDto(ChatMessage chatMessage) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(chatMessage.getId());
        dto.setContent(chatMessage.getContent());
        dto.setChatRoomId(chatMessage.getChatRoom().getId());
        dto.setNickname(chatMessage.getSender().getNickname());
        return dto;
    }

}
