package com.example.simplechatapp.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable // 이 클래스가 다른 클래스의 속성으로 사용될 수 있음을 나타냄
@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostImage {

    private String fileName;

//    @Setter
//    private int ord;

}
