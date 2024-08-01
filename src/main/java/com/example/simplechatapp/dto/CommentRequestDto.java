package com.example.simplechatapp.dto;


import com.example.simplechatapp.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentRequestDto {

    @NotNull
    private Long postId;

    @NotNull
    private String content;

    @NotNull
    private String createdBy;

    private Integer senderNo;
    private Long parentId;

    @Builder.Default
    private List<Comment> children = new ArrayList<>();


}
