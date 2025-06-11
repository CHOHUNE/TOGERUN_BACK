package com.example.simplechatapp.domain.like.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Getter
public class Like {
    private final Long id;
    private final Long userId;
    private final Long postId;
    private final LocalDateTime createdAt;
    private boolean active;

    // 기본 좋아요 생성
    public Like(Long postId, Long userId) {
        this.id = null; // 새로 생성되는 경우
        this.postId = postId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }

    //기본 좋아요 복원

    public Like(Long id, Long userId, Long postId, LocalDateTime createdAt, boolean active) {
        this.id = id;
        this.userId = userId;
        this.postId = postId;
        this.createdAt = createdAt;
        this.active = active;
    }

    //좋아요 토글
    public Like toggle() {
        return new Like(this.id, this.userId, this.postId, this.createdAt, this.active);
    }

    //비즈니스 로직 : 자신의 게시글인지 확인
    public boolean isSelfLike(Long postAuthorId) {
        return this.userId.equals(postAuthorId);
    }

    // 비즈니스 로직 : 좋아요 활성 유무
    public boolean isActiveLike() {
        return this.active;
    }

    //비즈니스 로직 : 첫 좋아요인지?
    public boolean isFirstActivation(Like previousState) {
        return previousState == null || (!previousState.active && this.active);
    }



}
