package com.example.simplechatapp.domain.like.application.port.out;

import com.example.simplechatapp.domain.like.domain.Like;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 아웃바운드 포트 인터페이스

public interface LikePort {
    Like save(Like like);
    Optional<Like> findByUserIdAndPostId(Long userId,Long postId);
}
