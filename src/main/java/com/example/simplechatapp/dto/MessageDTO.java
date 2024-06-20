package com.example.simplechatapp.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageDTO {

    private Long id;
    private String content;
    private LocalDateTime timestamp;
    private ChatRoomDTO chatRoom;
    private UserDTO sender;

}
