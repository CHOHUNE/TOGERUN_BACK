package com.example.simplechatapp.dto;


import com.example.simplechatapp.aop.proxy.NotifyInfo;
import com.example.simplechatapp.entity.Like;
import com.example.simplechatapp.entity.NotificationType;
import com.example.simplechatapp.entity.NotifyMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LikeDTO implements NotifyInfo {

    private Long id;
    private Long userId;
    private Long postId;
    private LocalDateTime createdAt;
    private boolean isActive;
    private Set<String> receivers;
    private String goUrlId;
    private boolean isFirstActiveLike;

    public static LikeDTO convertLikeToDto(Like like, boolean isFirstActiveLike) {
        Long postAuthorId = like.getPost().getUser().getId();
        Long likeUserId = like.getUser().getId();

        Set<String> receiversSet;
        if (postAuthorId.equals(likeUserId)) {
            receiversSet = Collections.emptySet(); // 자신의 게시물에 좋아요를 누른 경우
        } else {
            receiversSet = Set.of(like.getPost().getUser().getEmail());
        }

        return LikeDTO.builder()
                .id(like.getId())
                .userId(likeUserId)
                .postId(like.getPost().getId())
                .createdAt(like.getCreatedAt())
                .isActive(like.isActive())
                .receivers(receiversSet)
                .goUrlId("/post/" + like.getPost().getId())
                .isFirstActiveLike(isFirstActiveLike)
                .build();
    }

    @Override
    public Set<String> getReceiver() {
        return isFirstActiveLike ? receivers : null;
    }

    @Override
    public String getGoUrlId() {
        return isFirstActiveLike ?goUrlId:null;
    }

    @Override
    public NotificationType getNotificationType() {
        return isFirstActiveLike? NotificationType.LIKE:null;
    }

    @Override
    public NotifyMessage getNotifyMessage() {
        return isFirstActiveLike?NotifyMessage.NEW_LIKE:null;
    }
}
