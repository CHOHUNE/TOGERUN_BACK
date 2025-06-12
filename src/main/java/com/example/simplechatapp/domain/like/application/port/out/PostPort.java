package com.example.simplechatapp.domain.like.application.port.out;

import com.example.simplechatapp.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostPort extends JpaRepository<Post, Long>{
    boolean existById(Long postId);
    Long findAuthorIdByPostId(Long postId);
}
