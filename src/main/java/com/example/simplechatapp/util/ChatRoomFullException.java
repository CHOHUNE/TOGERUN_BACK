package com.example.simplechatapp.util;

public class ChatRoomFullException extends RuntimeException{

    private final Long postId;

    public ChatRoomFullException(String message, Long postId) {
        super(message);
        this.postId = postId;
    }

    public Long getPostId() {
        return postId;


    }
}
