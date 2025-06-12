package com.example.simplechatapp.domain.like.application.service;

import com.example.simplechatapp.annotation.DistributedLock;
import com.example.simplechatapp.domain.like.application.port.in.LikeResult;
import com.example.simplechatapp.domain.like.application.port.in.LikeUseCase;
import com.example.simplechatapp.domain.like.application.port.out.LikePort;
import com.example.simplechatapp.domain.like.application.port.out.PostPort;
import com.example.simplechatapp.domain.like.application.port.out.UserPort;
import com.example.simplechatapp.domain.like.domain.Like;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService implements LikeUseCase {

    private final LikePort likePort;
    private final UserPort userPort;
    private final PostPort postPort;


    @Override
    @Transactional
    @CacheEvict(value = "post", key = "#postId")
    @DistributedLock(key = "postId")
    public LikeResult toggleLike(String userEmail, Long postId) {

        // 1. 사용자 ID 조회
        Long userId = userPort.findUserIdByEmail(userEmail).orElseThrow(() -> new IllegalArgumentException("User not found" + userEmail));
        // 2. 게시글 존재 확인

        if (postPort.existById(postId)) {
            throw new IllegalArgumentException(postId + "post doesnt exist");
        }

        // 3. 게시글 작성자 ID 조회
        Long authorId = postPort.findAuthorIdByPostId(postId);

        // 4. 기존 좋아요 확인 ( empty heart or full heart)
        Like existingLike = likePort.findByUserIdAndPostId(userId, postId).orElse(null);

        // 5. 좋아요 토글 ( 도메인 로직 활용 )
        Like newLike;

        boolean isFirstActivation;

        if (existingLike == null) {
            newLike = new Like(userId, postId);
            isFirstActivation = true;
        } else {
            newLike = existingLike.toggle();
            isFirstActivation = newLike.isFirstActivation(existingLike);
        }

        // 6. 저장
        Like savedLike = likePort.save(newLike);

        // 7. 결과 반환
        return new LikeResult(
                savedLike.getId(),
                savedLike.getUserId(),
                savedLike.getPostId(),
                savedLike.isActive(),
                isFirstActivation&& savedLike.isActive(),
                savedLike.isSelfLike(authorId)
        );
    }
}

