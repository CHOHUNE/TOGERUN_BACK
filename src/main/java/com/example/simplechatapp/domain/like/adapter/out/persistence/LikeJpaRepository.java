package com.example.simplechatapp.domain.like.adapter.out.persistence;

import com.example.simplechatapp.domain.like.domain.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
interface LikeJpaRepository extends JpaRepository<LikeJpaEntity,Long> {

    Optional<LikeJpaEntity> findByUserIdAndPostId(Long userId, Long postId);
}
