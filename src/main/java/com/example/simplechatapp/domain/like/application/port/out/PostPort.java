package com.example.simplechatapp.domain.like.application.port.out;

import java.util.Optional;

public interface PostPort {
    boolean existById(Long postId);
    Optional<Long> findAuthorIdByPostId(Long postId);
}
