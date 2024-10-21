package com.example.simplechatapp.dto;


import com.example.simplechatapp.entity.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserChatRoomDTO {



    private Long chatRoomId;
    private Long postId;
    private String postTitle;
    private LocalDateTime meetingTime;
    private int participantCount;
    private int capacity;
    private LocalDateTime lastMessageTime;
    private String lastMessagePreview;
    private ActivityType activityType;


}
