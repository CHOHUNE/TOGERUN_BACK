package com.example.simplechatapp.util;

public class NicknameAlreadyExistException extends RuntimeException {

        public NicknameAlreadyExistException(String message) {
            super(message);
        }
}
