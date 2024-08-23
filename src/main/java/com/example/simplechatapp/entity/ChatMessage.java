package com.example.simplechatapp.entity;

import com.example.simplechatapp.dto.ChatMessageDTO;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "chat_messages")
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500, nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    @JsonBackReference
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime createdAt;


    @Enumerated(EnumType.STRING) // 번호로 사용시 추후 오류 발생을 미연에 방지
    private ChatMessageType chatMessageType;


//    private String email;

    public static ChatMessage chatMessageDtoToEntity(ChatMessageDTO chatMessageDTO, ChatRoom chatRoom, User user) {
        return ChatMessage.builder()
                .content(chatMessageDTO.getContent())
                .chatRoom(chatRoom)
                .user(user)
                .createdAt(chatMessageDTO.getCreatedAt())
                .chatMessageType(ChatMessageType.valueOf(chatMessageDTO.getChatMessageType())) // 추가
                .build();

    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
        if (chatRoom != null && chatRoom.getChatMessageList().contains(this)) {
            chatRoom.getChatMessageList().add(this);
        }
    }
}