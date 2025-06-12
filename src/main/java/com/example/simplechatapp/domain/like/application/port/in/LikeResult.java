package com.example.simplechatapp.domain.like.application.port.in;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/*
* UseCase 결과 객체
* */

@RequiredArgsConstructor
@Getter
public class LikeResult {

    private final Long likeId;
    private final Long userId;
    private final Long postId;
    private final boolean isActive;
    private final boolean isFirstActivation;
    private final boolean isSelfLike;

}
