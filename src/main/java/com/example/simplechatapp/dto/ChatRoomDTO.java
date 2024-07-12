package com.example.simplechatapp.dto;

import com.example.simplechatapp.entity.Post;
import com.example.simplechatapp.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
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

}
