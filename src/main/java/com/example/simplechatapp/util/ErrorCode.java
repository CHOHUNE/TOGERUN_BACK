package com.example.simplechatapp.util;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    POST_NOT_FOUND(NOT_FOUND, "해당 게시글  정보를 찾을 수 없습니다.", "022"),
    COMMENT_NOT_FOUND(NOT_FOUND, "해당 댓글 정보를 찾을 수 없습니다.", "023"),
    MAX_FILE_SIZE_EXCEEDED(BAD_REQUEST, "파일 크기가 초과되었습니다.", "025");

    private final HttpStatus httpStatus;
    private final String detail;
    private final String errorCode;

    public static CustomException throwPostNotFound() {
        throw new CustomException(POST_NOT_FOUND);
    }

    public static CustomException throwCommentNotFound() {
        throw new CustomException(COMMENT_NOT_FOUND);
    }

    public static CustomException throwMaxFileSizeExceeded() {
        throw new CustomException(MAX_FILE_SIZE_EXCEEDED);
    }





}
