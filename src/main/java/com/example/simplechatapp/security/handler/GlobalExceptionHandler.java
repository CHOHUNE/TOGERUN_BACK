package com.example.simplechatapp.security.handler;

import com.example.simplechatapp.util.LockException;
import com.example.simplechatapp.util.NicknameAlreadyExistException;
import org.springframework.dao.DataIntegrityViolationException;
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

    @ExceptionHandler(LockException.AcquisitionException.class)
    public ResponseEntity<ErrorResponse> handleLockAcquisitionException(LockException.AcquisitionException e) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT)
                .message("Failed to acquire lock for key: " + e.getLockKey())
                .redirect("/error")
                .errorStatus("LOCK_ACQUISITION_FAILED")
                .build();
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(LockException.ReleaseException.class)
    public ResponseEntity<ErrorResponse> handleLockReleaseException(LockException.ReleaseException e) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT)
                .message("Failed to release lock for key: " + e.getLockKey())
                .redirect("/error")
                .errorStatus("LOCK_RELEASE_FAILED")
                .build();
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NicknameAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleNicknameAlreadyExists(NicknameAlreadyExistException e) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT)
                .message(e.getMessage())
                .redirect("/member/modify")  // 적절한 리다이렉트 경로
                .errorStatus("NICKNAME_DUPLICATE")
                .build();
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT)
                .message("이미 사용중인 닉네임입니다.")
                .redirect("/member/modify")
                .errorStatus("DATA_INTEGRITY_VIOLATION")
                .build();
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

}