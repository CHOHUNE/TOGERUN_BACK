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

    public static LikeDTO convertLikeToDto(Like like) {
        return LikeDTO.builder()
                .id(like.getId())
                .userId(like.getUser().getId())
                .postId(like.getPost().getId())
                .createdAt(like.getCreatedAt())
                .isActive(like.isActive())
                .receivers(Set.of(like.getPost().getUser().getEmail()))
                .goUrlId("/api/post/"+like.getPost().getId())
                .build();
    }

    @Override
    public Set<String> getReceiver() {
        return receivers;
    }

    @Override
    public String getGoUrlId() {
        return goUrlId;
    }

    @Override
    public NotificationType getNotificationType() {
        return NotificationType.LIKE;
    }

    @Override
    public NotifyMessage getNotifyMessage() {
        return NotifyMessage.NEW_LIKE;
    }
}
