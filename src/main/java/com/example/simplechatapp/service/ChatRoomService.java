package com.example.simplechatapp.service;

import com.example.simplechatapp.dto.ChatMessageDTO;
import com.example.simplechatapp.entity.ChatMessage;
import com.example.simplechatapp.entity.ChatRoom;
import com.example.simplechatapp.entity.Post;
import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.repository.ChatMessageRepository;
import com.example.simplechatapp.repository.ChatRoomRepository;
import com.example.simplechatapp.repository.PostRepository;
import com.example.simplechatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;



//    public boolean isUserAllowedInChatRoom(Long chatRoomId, String email) {
//        User user = userRepository.findByEmail(email);
//
//        return chatRoomRepository.isUserAllowedInChatRoom(chatRoomId, user.getEmail());
//    }
// 단체 채팅방으로 변경 하면서 사용자 검증은 필요 없어짐 -> 단순 참여 유무 확인 후 추가 과정으로 대체

    public List<ChatMessageDTO> getMessageByChatRoomId(Long chatRoomId) {

        List<ChatMessage> chatMessages = chatMessageRepository.findMessagesByChatRoomId(chatRoomId);
        return chatMessages.stream()
                .map(ChatMessageDTO::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChatRoom joinChatRoom(Long postId, String userEmail) {

        // 클라이언트에서 버튼 클릭시 email 과 postId 를 전달 받고
        // 채팅방 유무 확인 후 생성 OR 참가
        // 기존 참가 여부 확인 후 참가 OR 불러오기

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new IllegalArgumentException(" POST NOT FOUND ")
        );
        // 포스트가 없으면 예외 처리


// 1번 채팅방 생성 유무 검증
        ChatRoom chatRoom = chatRoomRepository.findByPost(post)
                .orElseGet(() -> {
                    ChatRoom newChatRoom = new ChatRoom();
                    newChatRoom.setPost(post);
                    newChatRoom.addParticipant(post.getUser()); // 게시글 작성자를 자동으로 추가
                    return chatRoomRepository.save(newChatRoom);
                });
        //게시판 생성시 채팅방 생성은 비동기 처리 -> 채팅방에 등록된 포스트를 찾은 후 없으면 새로 생성

// 2번 참가자 추가 유무 검증

        User user = userRepository.findByEmail(userEmail);
        //해당 유저를 찾고

        if (!chatRoom.hasParticipant(user)) {
            chatRoom.addParticipant(user);

        }else{
//            throw new IllegalArgumentException("이미 참가한 사용자입니다.");
            // 예외처리시 메소드가 즉시 중단되므로 로그 처리 후 리턴
            log.info("이미 참가한 사용자입니다.");
        }

        // addParticipant 에 검증과정

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
