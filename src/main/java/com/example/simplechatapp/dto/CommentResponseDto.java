package com.example.simplechatapp.dto;


import com.example.simplechatapp.aop.proxy.NotifyInfo;
import com.example.simplechatapp.entity.Comment;
import com.example.simplechatapp.entity.NotificationType;
import com.example.simplechatapp.entity.NotifyMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponseDto implements NotifyInfo {

    private Long id;
    private Long postId;

    @NotNull
    private String content;

    @NotNull
    private String createdBy;

    private Long parentId;
    private Set<String> receivers;
    private String goUrlId;
    private String name;
    private String img;

    @Builder.Default
    private List<CommentResponseDto> children = new ArrayList<>();
    private LocalDateTime createdAt;


//    public CommentResponseDto(Long id, Long postId, String content, String createdBy, Long parentId, LocalDateTime createdAt) {
//        this.id = id;
//        this.postId = postId;
//        this.content = content;
//        this.createdBy = createdBy;
//        this.parentId = parentId;
//        this.createdAt = createdAt;
//    }

    public static CommentResponseDto convertCommentToDto(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .content(comment.getContent())
                .createdBy(comment.getCreatedBy())
                .parentId(comment.getParent() == null ? null : comment.getParent().getId())
                .createdAt(comment.getCreatedAt())
                .receivers(comment.getPost().getUser().getEmail() ==comment.getCreatedBy() ? Set.of() : Set.of(comment.getPost().getUser().getEmail()))
                // 포스트 작성자와 코멘트 게재자가 같을 경우에 알람이 가지 않는다.
                // 단순히 포스트 작성자에게만 알람이 가게 해야 하나 아니면 댓글을 달은 당사자 전부에게 가야 하나 고민 중
                .goUrlId("/post/"+comment.getPost().getId())
                .name(comment.getName())
                .img(comment.getImg())
                .build();
    }

    // NotificationType 이 Dto 에 없는 이유는 Service 에서 getNotificationType() 을 직접 호출


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
        return NotificationType.COMMENT;
    }

    @Override
    public NotifyMessage getNotifyMessage() {
        return NotifyMessage.NEW_COMMENT;
    }

    @Override
    public Long getPostId() {
        return postId;
    }
}
