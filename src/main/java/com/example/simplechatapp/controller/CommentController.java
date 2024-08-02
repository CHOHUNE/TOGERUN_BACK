package com.example.simplechatapp.controller;

import com.example.simplechatapp.dto.CommentRequestDto;
import com.example.simplechatapp.dto.CommentResponseDto;
import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.repository.PostRepository;
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



    @PostMapping // postId 는 CommentRequestDto 에 포함 되어 있어서 파라메터로 받지 않음
    public ResponseEntity<CommentResponseDto> createComment(@RequestBody CommentRequestDto commentRequestDto, @AuthenticationPrincipal UserDTO principal) {

        CommentResponseDto commentResponseDto = commentService.createComment(commentRequestDto, principal);
        return ResponseEntity.ok().body(commentResponseDto);

    }

    @PutMapping
    public ResponseEntity<CommentResponseDto> modifyComment(@RequestBody CommentRequestDto commentRequestDto,@AuthenticationPrincipal UserDTO principal) {
        CommentResponseDto commentResponseDto = commentService.modifyComment(commentRequestDto, principal);
        return ResponseEntity.ok().body(commentResponseDto);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<List<CommentResponseDto>> getComment(@PathVariable Long postId) {
        List<CommentResponseDto> commentResponseDtoList = commentService.findCommentListByPostId(postId);
        return ResponseEntity.ok().body(commentResponseDtoList);
    }

    @DeleteMapping("/{commentId}")
    public void deleteSnsComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
    }
}
