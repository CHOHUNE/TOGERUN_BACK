package com.example.simplechatapp.security.handler;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class ErrorResponse {
    private final HttpStatus status;
    private final String message;
    private final String redirect;
    @Builder.Default
    private final String errorStatus = "ERROR";

}