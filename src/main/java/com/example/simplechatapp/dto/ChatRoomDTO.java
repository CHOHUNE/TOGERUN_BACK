package com.example.simplechatapp.dto;

import com.example.simplechatapp.entity.Post;
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
    private Post post;

    private Set<User> participants = new HashSet<>();
    private int participantCount; // 새로 추가된 필드

    public void addParticipant(User user) {
        participants.add(user);
        participantCount = participants.size();
    }

    public void removeParticipant(User user) {
        participants.remove(user);
        participantCount = participants.size();
    }

    public boolean isFull() {
        return participantCount >= post.getCapacity();
    }

}
