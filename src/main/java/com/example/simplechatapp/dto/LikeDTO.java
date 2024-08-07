package com.example.simplechatapp.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LikeDTO {

    private Long id;
    private Long userId;
    private Long postId;
    private LocalDateTime createdAt;
    private boolean isActive;

}
