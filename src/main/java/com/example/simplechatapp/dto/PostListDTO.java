package com.example.simplechatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostListDTO {
    private Long id;
    private String title;
    private String nickname;
    private LocalDate localDate;
    private Long likeCount;
    private String placeName;
    private Long viewCount;
    private Boolean participateFlag;
    private String roadName;

}