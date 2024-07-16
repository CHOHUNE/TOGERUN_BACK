package com.example.simplechatapp.repository;

import com.example.simplechatapp.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>{

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.post.id = :postId ORDER BY cm.id ASC")
    List<ChatMessage> findChatMessageByPostId(@Param("postId") Long postId);

}
