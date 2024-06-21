package com.example.simplechatapp.Service;

import com.example.simplechatapp.dto.ChatRoomDTO;
import com.example.simplechatapp.entity.ChatRoom;
import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.repository.ChatRoomRepository;
import com.example.simplechatapp.repository.UserRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Builder
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;


    @Transactional
    public ChatRoomDTO createOrGetChatRoom(String user1Email, String user2Email){

        ChatRoom exisitingChatRoom = chatRoomRepository.findByUserEmails(user1Email, user2Email);

        if(exisitingChatRoom != null){
            return  convertToDTO(exisitingChatRoom);
        }

        User user1= userRepository.findById(user1Email).orElseThrow(()->new IllegalArgumentException("User with email "+user1Email+" not found"));
        User user2= userRepository.findById(user2Email).orElseThrow(()->new IllegalArgumentException("User with email "+user1Email+" not found"));

        ChatRoom chatRoom = new ChatRoom();

        chatRoom.setUser1(user1);
        chatRoom.setUser2(user2);

        chatRoomRepository.save(chatRoom);

        return convertToDTO(chatRoom);

    }




    private ChatRoomDTO convertToDTO(ChatRoom chatRoom) {

    return ChatRoomDTO.builder()
                .id(chatRoom.getId())
                .id(chatRoom.getId())
                .user1Email(chatRoom.getUser1().getEmail())
                .user2Email(chatRoom.getUser2().getEmail())
                .build();
    }


}
