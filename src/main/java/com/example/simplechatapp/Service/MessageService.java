package com.example.simplechatapp.Service;

import com.example.simplechatapp.entity.ChatRoom;
import com.example.simplechatapp.entity.Message;
import com.example.simplechatapp.repository.ChatRoomRepository;
import com.example.simplechatapp.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;

    public List<Message> findAllMessage() {

        return messageRepository.findAll();

    }

    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }


}
