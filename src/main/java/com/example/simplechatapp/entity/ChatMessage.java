package com.example.simplechatapp.entity;


import com.example.simplechatapp.dto.ChatMessageDTO;
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

    @Column(length = 1000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime createdAt;

//    private String email;

    public static ChatMessage chatMessageDtoToEntity(ChatMessageDTO chatMessageDTO, ChatRoom chatRoom, User user) {
        return ChatMessage.builder()
                .content(chatMessageDTO.getContent())
                .chatRoom(chatRoom)
                .user(user)
                .createdAt(chatMessageDTO.getCreatedAt())
                .build();
    }

//    public ChatMessage(String content, ChatRoom chatRoom, User user) {
//        this.content = content;
//        this.chatRoom = chatRoom;
//        this.nickname = user;
//    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
        if (chatRoom != null && chatRoom.getChatMessageList().contains(this)) {
            chatRoom.getChatMessageList().add(this);
        }
    }



}
