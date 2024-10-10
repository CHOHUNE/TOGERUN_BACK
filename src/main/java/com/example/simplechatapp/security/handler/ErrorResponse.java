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

    public static class ErrorResponseBuilder {
        private HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public int getStatusCode() {
        return status.value();
    }

    public String getStatusName() {
        return status.name();
    }
}