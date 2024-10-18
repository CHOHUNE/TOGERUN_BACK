package com.example.simplechatapp.security.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .message(e.getMessage())
                .redirect("/")
                .build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN)
                .message(e.getMessage() != null ? e.getMessage() : "접근 권한이 없습니다.")
                .redirect("/error")
                .errorStatus("ACCESS_DENIED")
                .build();
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED)
                .message("인증에 실패했습니다.")
                .redirect("/member/login")
                .errorStatus("AUTHENTICATION_FAILED")
                .build();
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientAuthenticationException(InsufficientAuthenticationException e) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN)
                .message("로그인이 필요하거나 관리자 권한이 필요합니다.")
                .redirect("/member/login")
                .errorStatus("INSUFFICIENT_AUTHENTICATION")
                .build();
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }
}