package com.example.simplechatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomDTO {

    private Long id;
    private UserDTO user1;
    private UserDTO user2;

}
