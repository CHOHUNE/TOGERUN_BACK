package com.example.simplechatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PostDTO {
        private Long id;
        private String title;
        private String content;
        private Long userId;
        private String nickname;
        private LocalDate localDate;
        private boolean delFlag;
        private boolean isFavorite;
        private Long likeCount;
        private boolean isLike;
        private String placeName;
        private Double latitude;
        private Double longitude;
        private LocalDateTime meetingTime;


//        @Builder.Default // Builder.Default 를 사용하면 초기화한 상태를 직접 설정할 수 있다.
//        // 가령 List 를 초기화 하지 않으면 null 값이 들어가고, nullPointException 등이 발생될 수 있다.
//        private List<MultipartFile> file = new ArrayList<>();

        @Builder.Default
        private List<String> imageList = new ArrayList<>();


        // Querydsl용 생성자 (imageList 제외)
        public PostDTO(Long id, String title, String content, Long userId, String nickname,
                       LocalDate localDate, Boolean delFlag, Boolean isFavorite, Long likeCount,
                       Boolean isLike, String placeName, Double latitude, Double longitude,
                       LocalDateTime meetingTime) {
                this(id, title, content, userId, nickname, localDate, delFlag, isFavorite, likeCount,
                        isLike, placeName, latitude, longitude, meetingTime, new ArrayList<>());
        }

        // 모든 필드를 포함하는 생성자
        public PostDTO(Long id, String title, String content, Long userId, String nickname,
                       LocalDate localDate, Boolean delFlag, Boolean isFavorite, Long likeCount,
                       Boolean isLike, String placeName, Double latitude, Double longitude,
                       LocalDateTime meetingTime, List<String> imageList) {
                this.id = id;
                this.title = title;
                this.content = content;
                this.userId = userId;
                this.nickname = nickname;
                this.localDate = localDate;
                this.delFlag = delFlag != null ? delFlag : false;
                this.isFavorite = isFavorite != null ? isFavorite : false;
                this.likeCount = likeCount;
                this.isLike = isLike != null ? isLike : false;
                this.placeName = placeName;
                this.latitude = latitude;
                this.longitude = longitude;
                this.meetingTime = meetingTime;
                this.imageList = imageList != null ? imageList : new ArrayList<>();

        }
}