package com.example.simplechatapp.dto;


import com.example.simplechatapp.entity.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteDTO {

    private Long id;
    private Long userId;
    private Long postId;
    private LocalDateTime createdAt;
    private boolean isActive;
    private String postTitle;
    private LocalDate localDate;
    private LocalDateTime meetingTime;
    private ActivityType activityType;
    private Integer capacity;
    private String placeName;
    private String createdBy;
    private boolean participateFlag;

}
