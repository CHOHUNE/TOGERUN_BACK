package com.example.simplechatapp.entity;

import lombok.Getter;


@Getter
public enum NotifyMessage {

    CHAT_APP_ALERT("새로운 채팅 메세지가 있습니다.");
    private final String message;

    NotifyMessage(String message) {
        this.message = message;
    }

}
