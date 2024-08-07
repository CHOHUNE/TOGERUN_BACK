package com.example.simplechatapp.repository;

import com.example.simplechatapp.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    void deleteByUserIdAndPostId(Long userId, Long postId);
    boolean existsByUserIdAndPostId(Long userId, Long postId);
    int countByPostId(Long postId);

    Optional<Favorite> findByUserIdAndPostId(Long userId, Long postId);
}
