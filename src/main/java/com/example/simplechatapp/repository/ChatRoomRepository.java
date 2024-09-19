package com.example.simplechatapp.repository;

import com.example.simplechatapp.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.post.id =?1")
    Optional<ChatRoom> findByPostId(@Param("postId") Long postId);

}

