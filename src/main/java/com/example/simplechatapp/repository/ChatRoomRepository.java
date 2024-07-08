package com.example.simplechatapp.repository;

import com.example.simplechatapp.entity.ChatRoom;
import com.example.simplechatapp.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.writer.email = ?1 AND cr.subscriber.email = ?2")
    ChatRoom findByUserEmails(String user1Email, String user2Email);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM ChatRoom c " +
           "WHERE c.id = :chatRoomId AND (c.writer.email = :userId OR c.subscriber.email = :email)")
    boolean isUserAllowedInChatRoom(@Param("chatRoomId") Long chatRoomId, @Param("userId") String email);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.post =?1")
    Optional<ChatRoom> findByPost(Post post);
}
