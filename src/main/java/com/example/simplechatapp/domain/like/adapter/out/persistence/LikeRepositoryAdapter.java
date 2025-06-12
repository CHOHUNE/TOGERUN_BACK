package com.example.simplechatapp.domain.like.adapter.out.persistence;

import com.example.simplechatapp.domain.like.application.port.out.LikePort;
import com.example.simplechatapp.domain.like.domain.Like;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LikeRepositoryAdapter implements LikePort {

    private final LikeJpaRepository likeJpaRepository;


    @Override
    public Like save(Like like) {
        LikeJpaEntity likeJpaEntity = mapToEntity(like);
        LikeJpaEntity saved = likeJpaRepository.save(likeJpaEntity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<Like> findByUserIdAndPostId(Long userId, Long postId) {
        return likeJpaRepository.findByUserIdAndPostId(userId, postId)
                .map(this::mapToDomain);
    }

    // domain -> entity
    private LikeJpaEntity mapToEntity(Like like) {
        return LikeJpaEntity.builder().id(like.getId()).userId(like.getUserId()).postId(like.getPostId()).isActive(like.isActiveLike()).createdAt(like.getCreatedAt()).build();
    }

    //JPA 엔티티 -> 도메인
    private Like mapToDomain(LikeJpaEntity entity) {
        return new Like(entity.getPostId(), entity.getUserId(), entity.getPostId(), entity.getCreatedAt(), entity.isActive());
    }


}
