package com.example.simplechatapp.dto;

import com.example.simplechatapp.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDTO {

    private Long id;
    private Long postId;
    private Set<User> participants = new HashSet<>();
    private int participantCount; // 새로 추가된 필드

    private boolean isParticipant;
    private boolean canJoin;

}
