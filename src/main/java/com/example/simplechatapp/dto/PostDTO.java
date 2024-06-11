package com.example.simplechatapp.dto;


import com.example.simplechatapp.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostDTO {

        private Long id;
        private String title;
        private String content;
        private User user;

        private LocalDate localDate;

}
