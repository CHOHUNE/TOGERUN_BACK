package com.example.simplechatapp.controller;

import com.example.simplechatapp.Service.ChatRoomService;
import com.example.simplechatapp.Service.MessageService;
import com.example.simplechatapp.entity.ChatRoom;
import com.example.simplechatapp.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final MessageService messageService;


//    @GetMapping("/rooms")
//    public List<ChatRoom> getAllChatRooms() {
//        return chatRoomService.findAll();
//    }
//
//    @GetMapping("/{id}")
//    public ChatRoom getChatRoomById(@PathVariable Long id) {
//
//        return chatRoomService.findById(id);
//    }
//

    @GetMapping
    public List<Message> getMessageByChatRoomId() {

        return messageService.findAllMessage();
    }

    @MessageMapping("/sendMessage")
    @SendTo("/topic/messages")
    public Message sendMessage(Message message) {
        return messageService.saveMessage(message);
    }

}
