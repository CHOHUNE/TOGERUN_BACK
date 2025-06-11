package com.example.simplechatapp.domain.like.application.port.in;

// 포트 인터페이스
public interface LikeUserCase {
    LikeResult toggleLike(String userEmail, Long postId);
}
