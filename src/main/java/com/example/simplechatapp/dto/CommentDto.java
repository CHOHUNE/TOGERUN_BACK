package com.example.simplechatapp.dto;

import com.example.simplechatapp.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentDto {

    private Long id;

    private String content;
    private String email;
    private Long postId;
    private Long writerId;
    private Long parentId;
    private List<CommentDto> children = new ArrayList<>();

    public static CommentDto convertCommentToDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .email(comment.getUser().getEmail())
                .postId(comment.getPost().getId())
                .writerId(comment.getUser().getId())
                .parentId(comment.getParent() == null ? null : comment.getParent().getId())
                .children(comment.getChildren().stream().map(CommentDto::convertCommentToDto).toList())
                .build();
    }
}
