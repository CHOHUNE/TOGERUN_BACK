package com.example.simplechatapp.domain.like.application.port.out;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface PostPort {
    boolean existById(Long postId);
    Optional<Long> findAuthorIdByPostId(Long postId);
}
