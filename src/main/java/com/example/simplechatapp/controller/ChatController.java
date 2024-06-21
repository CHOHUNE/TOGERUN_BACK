package com.example.simplechatapp.controller;

import com.example.simplechatapp.Service.ChatRoomService;
import com.example.simplechatapp.Service.MessageService;
import com.example.simplechatapp.dto.ChatRoomDTO;
import com.example.simplechatapp.entity.ChatRoom;
import com.example.simplechatapp.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;


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

//TODO : 채팅방 생성, 채팅방 조회

    @PostMapping("/create")
    public ResponseEntity<ChatRoomDTO> createChatRoom(@RequestParam("user1Email")String user1Email, @RequestParam("user2Email")String user2Email) {

        try {
            ChatRoomDTO createdChatRoom = chatRoomService.createOrGetChatRoom(user1Email, user2Email);
            return new ResponseEntity<>(createdChatRoom, HttpStatus.CREATED);
        } catch (Exception e) {

            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

//    @GetMapping("/get")
//    public ResponseEntity<ChatRoomDTO> getChatRoom(@RequestParam("user1Email")String user1Email, @RequestParam("user2Email")String user2Email) {
//
//        try {
//            ChatRoomDTO chatRoom = chatRoomService.getChatRoom(user1Email, user2Email);
//            if (chatRoom != null) {
//                return new ResponseEntity<>(chatRoom, HttpStatus.OK);
//            } else {
//                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//            }
//        } catch (Exception e) {
//
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
}
