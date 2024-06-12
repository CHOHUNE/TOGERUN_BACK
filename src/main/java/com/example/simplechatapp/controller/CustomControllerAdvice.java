package com.example.simplechatapp.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.NoSuchElementException;


// Restful 서비스에서 발생할 수 있는 API 를 전역적으로 관리 한다.
// 예외 발생시 @ResponseBody 를 활용해 JSON 으로 응답 하거나
// ResponseEntity 를 활용해 HTTP 응답 코드를 설정 할 수 있다.
// ExceptionHandler 를 이용해 특정 예외를 처리

@RestControllerAdvice
public class CustomControllerAdvice {

    @ExceptionHandler(NoSuchElementException.class)
    // 요청한 리소스가 없을 때 발생하는 에러
    public ResponseEntity<?> noExist(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message{}",e.getMessage()));
        // 500 에러를 -> 404 코드로 변환
    }



    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> notExist(MethodArgumentNotValidException e) {
    // 클라이언트가 보낸 요청의 매개변수가 유효하지 않을 때 발생
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(Map.of("messages{}", e.getMessage()));

    }

    // 해당 컨트롤러의 의도 : 웹 애플리케이션 화면에서 사용자가 좀 더 쉬운 오류 메세지로 편의성을 돕기 위해





}
