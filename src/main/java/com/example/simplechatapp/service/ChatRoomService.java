package com.example.simplechatapp.service;

import com.example.simplechatapp.entity.ChatRoom;
import com.example.simplechatapp.entity.Post;
import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.repository.ChatRoomRepository;
import com.example.simplechatapp.repository.PostRepository;
import com.example.simplechatapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private UserRepository userRepository;
    private PostRepository postRepository;

    public Long getChatRoomId(Long postId, Long writerId, Long subscriberId) {

        return Math.min(postId, writerId) + Math.max(postId, writerId) + subscriberId;
    }

    public boolean isUserAllowedInChatRoom(Long chatRoomId, String email) {
        User user = userRepository.findByEmail(email);

        return chatRoomRepository.isUserAllowedInChatRoom(chatRoomId, user.getEmail());
    }

    @Transactional
    public ChatRoom joinChatRoom(Long postId, String userEmail) {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new IllegalArgumentException(" POST NOT FOUND ")
        );

        User user = userRepository.findByEmail(userEmail);

        ChatRoom chatRoom = chatRoomRepository.findByPost(post)
                .orElseGet(() -> {
                    ChatRoom newChatRoom = new ChatRoom();
                    newChatRoom.setPost(post);
                    newChatRoom.addParticipant(post.getUser()); // 게시글 작성자를 자동으로 추가
                    return chatRoomRepository.save(newChatRoom);
                });

        chatRoom.addParticipant(user);

        return chatRoomRepository.save(chatRoom);

    }




//    @Transactional
//    public ChatRoom createChatRoom(Long postId) {
//        Post post = postRepository.findById(postId).orElseThrow(()->new IllegalArgumentException("POST NOT FOUND"));
//
//        ChatRoom chatRoom = new ChatRoom();
//        chatRoom.setPost(post);
//        chatRoom.addParticipant(post.getUser()); // 작성자는 채팅 룸에 자동으로 추가
//
//        return chatRoomRepository.save(chatRoom);
//
//    }

//    @Transactional
//    public void addParticipantToChatRoom(Long chatRoomId, String email) {
//        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(
//                ()->new EntityNotFoundException("Chat room not found"));
//        User user = userRepository.findByEmail(email);
//
//        chatRoom.addParticipant(user);
//
//        chatRoomRepository.save(chatRoom);
//
//    }




//    public boolean isUserAllowedInChatRoom(Long chatRoomId, String email) {
//
////        1. 채팅방 조회
//        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
//                .orElseThrow(() -> new EntityNotFoundException("Chat room not found"));
//
////        2.사용자 조회
//        User user = userRepository.findByEmail(email);
//
//
//        return isParticipant(chatRoom, user);
//
//    }
//
//    private boolean isParticipant(ChatRoom chatRoom, User user) {
//        return user.getEmail().equals(chatRoom.getWriter().getEmail()) ||
//               user.getEmail().equals(chatRoom.getSubscriber().getEmail());
//
//    }
    // 객체 생성 대신 단일문의 쿼리를 대신하기로 결정 -> 불필요한 객체 생성 피하기, 데이터베이스 부하 줄이기
}
