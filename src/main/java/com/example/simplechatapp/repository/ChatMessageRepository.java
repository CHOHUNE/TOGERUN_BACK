package com.example.simplechatapp.repository;

import com.example.simplechatapp.entity.ChatMessage;
import com.example.simplechatapp.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>{


    @Query("SELECT cm.chatRoom FROM ChatMessage cm WHERE cm.content LIKE %:message% AND cm.chatRoom.id = :chatRoomId")
    ChatRoom findChatRoomByMessageAndChatRoomId(String message, Long chatRoomId);

}
