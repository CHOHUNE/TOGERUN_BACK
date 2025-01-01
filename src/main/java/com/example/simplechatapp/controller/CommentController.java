package com.example.simplechatapp.controller;

import com.example.simplechatapp.annotation.NeedNotify;
import com.example.simplechatapp.annotation.RateLimit;
import com.example.simplechatapp.dto.CommentRequestDTO;
import com.example.simplechatapp.dto.CommentResponseDTO;
import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comment")
public class CommentController {

    private final CommentService commentService;


    @RateLimit(maxRequests = 20, duration = 600)
    @PostMapping // postId 는 CommentRequestDto 에 포함 되어 있어서 파라메터로 받지 않음
    @NeedNotify
    public ResponseEntity<CommentResponseDTO> createComment(@RequestBody CommentRequestDTO commentRequestDto, @AuthenticationPrincipal UserDTO principal) {

        CommentResponseDTO commentResponseDto = commentService.createComment(commentRequestDto, principal);
        return ResponseEntity.ok().body(commentResponseDto);
    }

    @PutMapping
    public ResponseEntity<CommentResponseDTO> modifyComment(@RequestBody CommentRequestDTO commentRequestDto, @AuthenticationPrincipal UserDTO principal) {
        CommentResponseDTO commentResponseDto = commentService.modifyComment(commentRequestDto, principal);
        return ResponseEntity.ok().body(commentResponseDto);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<List<CommentResponseDTO>> getComment(@PathVariable Long postId) {
        List<CommentResponseDTO> commentResponseDTOList = commentService.findCommentListByPostId(postId);
        return ResponseEntity.ok().body(commentResponseDTOList);
    }

    @DeleteMapping("/{commentId}")
    public Long deleteSnsComment(@PathVariable Long commentId) {

         return commentService.deleteComment(commentId);
    }
}
