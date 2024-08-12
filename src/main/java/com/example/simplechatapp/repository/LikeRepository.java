package com.example.simplechatapp.repository;

import com.example.simplechatapp.entity.Like;
import com.example.simplechatapp.entity.Post;
import com.example.simplechatapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    void deleteByUserIdAndPostId(Long userId, Long postId);
    boolean existsByUserIdAndPostId(Long userId, Long postId);
    int countByPostId(Long postId);

    Optional<Like> findByUserIdAndPostId(Long userId, Long postId);

}