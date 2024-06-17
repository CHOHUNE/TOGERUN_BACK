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
        private User user;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate localDate;

        private boolean delFlag;

        private List<MultipartFile> file = new ArrayList<>();
        private List<String> uploadFileName = new ArrayList<>();


}
