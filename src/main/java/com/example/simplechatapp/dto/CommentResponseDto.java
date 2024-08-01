package com.example.simplechatapp.dto;


import com.example.simplechatapp.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponseDto {

    private Long id;
    private Long postId;

    @NotNull
    private String content;

    @NotNull
    private String createdBy;

    private Long parentId;

    private List<CommentResponseDto> children = new ArrayList<>();

    private LocalDateTime createdAt;

    public CommentResponseDto(Long id, Long postId, String content, String createdBy, Long parentId, LocalDateTime createdAt) {
        this.id = id;
        this.postId = postId;
        this.content = content;
        this.createdBy = createdBy;
        this.parentId = parentId;
        this.createdAt = createdAt;
    }

    public static CommentResponseDto convertCommentToDto(Comment comment) {
        return new CommentResponseDto(comment.getId(), comment.getPost().getId(), comment.getContent(), comment.getCreatedBy(), comment.getParent() != null ? comment.getParent().getId() : null, comment.getCreatedAt());
    }


}
