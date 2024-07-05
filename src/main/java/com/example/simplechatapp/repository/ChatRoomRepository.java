package com.example.simplechatapp.repository;

import com.example.simplechatapp.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.user1.email = ?1 AND cr.user2.email = ?2")
    ChatRoom findByUserEmails(String user1Email, String user2Email);



}
