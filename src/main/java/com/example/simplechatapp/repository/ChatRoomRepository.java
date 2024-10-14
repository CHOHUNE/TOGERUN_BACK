package com.example.simplechatapp.repository;

import com.example.simplechatapp.dto.UserChatRoomDTO;
import com.example.simplechatapp.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.post.id =?1")
    Optional<ChatRoom> findByPostId(@Param("postId") Long postId);

    @Query("SELECT NEW com.example.simplechatapp.dto.UserChatRoomDTO(" +
           "cr.id, " +                  // chatRoomId
           "p.id, " +                   // postId
           "p.title, " +                // postTitle
           "p.meetingTime, " +          // meetingTime
           "cr.participantCount, " +    // participantCount
           "p.capacity, " +             // capacity
           "MAX(cm.createdAt), " +      // lastMessageTime
           "SUBSTRING(MAX(CASE WHEN cm.id = (SELECT MAX(cm2.id) FROM ChatMessage cm2 WHERE cm2.chatRoom = cr) THEN cm.content ELSE NULL END), 1, 30), " + // lastMessagePreview
           "cr.activityType) " +        // activityType
           "FROM User u " +
           "JOIN u.joinedChatRoom cr " +
           "JOIN cr.post p " +
           "LEFT JOIN cr.chatMessageList cm " +
           "WHERE u.email = :email " +
           "GROUP BY cr.id, p.id, p.title, p.meetingTime, cr.participantCount, p.capacity, cr.activityType")
    List<UserChatRoomDTO> findUserChatRoomDTOs(@Param("email") String email);
}


