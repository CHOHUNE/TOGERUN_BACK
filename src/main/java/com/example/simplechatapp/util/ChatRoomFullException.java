package com.example.simplechatapp.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ChatRoomFullException extends RuntimeException{

    private final Long postId;


}
