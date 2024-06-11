package com.example.simplechatapp.repository;

import com.example.simplechatapp.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message,Long> {

    List<Message> findByChatRoomId(Long chatRoomId);
}
