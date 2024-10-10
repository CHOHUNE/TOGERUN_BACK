package com.example.simplechatapp.util;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomException extends RuntimeException{

    public CustomException(String message) {
        super(message);
    }

}
