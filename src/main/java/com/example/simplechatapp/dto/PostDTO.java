package com.example.simplechatapp.dto;


import com.example.simplechatapp.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostDTO {

        private Long id;
        private String title;
        private String content;
        private Long userId;
        private String nickname;
        // User 객체를 직접 넣었다가 보안 & 필요한 데이터만 보내기 위해 id와 닉네임으로 변경


        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate localDate;

        private boolean delFlag;

        @Builder.Default // Builder.Default 를 사용하면 초기화한 상태를 직접 설정할 수 있다.
        // 가령 List 를 초기화 하지 않으면 null 값이 들어가고, nullPointException 등이 발생될 수 있다.
        private List<MultipartFile> file = new ArrayList<>();
        @Builder.Default
        private List<String> uploadFileName = new ArrayList<>();


}
