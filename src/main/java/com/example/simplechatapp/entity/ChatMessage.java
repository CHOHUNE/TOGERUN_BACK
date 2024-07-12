package com.example.simplechatapp.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@NoArgsConstructor
@Data
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    private User sender;

    public ChatMessage(String content, ChatRoom chatRoom, User user) {
        this.content = content;
        this.chatRoom = chatRoom;
        this.sender = user;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
        if (chatRoom != null && chatRoom.getChatMessageList().contains(this)) {
            chatRoom.getChatMessageList().add(this);
        }
    }



}
