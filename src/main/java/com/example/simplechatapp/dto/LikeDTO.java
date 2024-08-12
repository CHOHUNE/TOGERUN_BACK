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

    public static LikeDTO convertLikeToDto(Like like,boolean isFirstActiveLike) {
        return LikeDTO.builder()
                .id(like.getId())
                .userId(like.getUser().getId())
                .postId(like.getPost().getId())
                .createdAt(like.getCreatedAt())
                .isActive(like.isActive())
                .receivers(Set.of(like.getPost().getUser().getEmail()))
                .goUrlId("/post/"+like.getPost().getId())
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
