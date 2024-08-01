package com.example.simplechatapp.controller;

import com.example.simplechatapp.dto.CommentRequestDto;
import com.example.simplechatapp.dto.CommentResponseDto;
import com.example.simplechatapp.repository.PostRepository;
import com.example.simplechatapp.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final PostRepository postRepository;


    @PostMapping("/comment")
    public ResponseEntity<CommentResponseDto> createComment(@RequestBody CommentRequestDto commentRequestDto) {

        CommentResponseDto commentResponseDto = commentService.createComment(commentRequestDto);
        return ResponseEntity.ok().body(commentResponseDto);
    }

    @GetMapping("/comment/{postId}")
    public ResponseEntity<List<CommentResponseDto>> getComment(@PathVariable Long postId) {
        List<CommentResponseDto> commentResponseDtoList = commentService.findCommentListByPostId(postId);
        return ResponseEntity.ok().body(commentResponseDtoList);
    }

    @DeleteMapping("/comment/{commentId}")
    public void deleteSnsComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
    }
}
