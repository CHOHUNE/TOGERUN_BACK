package com.example.simplechatapp;


import com.example.simplechatapp.dto.CommentRequestDto;
import com.example.simplechatapp.dto.CommentResponseDto;
import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.entity.Comment;
import com.example.simplechatapp.entity.Post;
import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.repository.CommentRepository;
import com.example.simplechatapp.repository.PostRepository;
import com.example.simplechatapp.service.CommentService;

import com.example.simplechatapp.util.CustomException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    CommentRepository commentRepository;

    @Mock // Mock 객체 생성
    PostRepository postRepository;

    @InjectMocks // @Mock으로 등록된 객체를 주입받는다.
    CommentService commentService;

    @Test
    @DisplayName("댓글 작성 단위 테스트")
    void createCommentTest() {

        //given

        CommentRequestDto commentRequestDto = CommentRequestDto.builder().
                post_id(1L)
                .content("댓글 작성 테스트")
                .createdBy("작성자")
                .build();

        CommentResponseDto commentResponseDto = CommentResponseDto.builder()
                .id(1L)
                .content("댓글 작성 테스트")
                .createdBy("작성자")
                .build();

        Post post = Post.builder().
                id(1L)
                .title("제목")
                .content("댓글 작성 테스트")
                .build();

        //when
        when(postRepository.findById(1L)).thenReturn(Optional.ofNullable(post));
        UserDTO principal = new UserDTO(1L, "email", "password", "nickname", false, List.of());


        CommentResponseDto expectedDto = commentService.createComment(commentRequestDto, principal);
        Assertions.assertThat(expectedDto.getCreatedBy()).isEqualTo("작성자");

        verify(commentRepository, times(1)).save(any());


    }

    @Test
    @DisplayName("댓글 작성 예외 테스트 - 게시글이 없을 경우")
    void createCommentExceptionTest() {

        //given
        CommentRequestDto commentRequestDto = CommentRequestDto.builder()
                .post_id(1L)
                .content("댓글 작성 테스트")
                .createdBy("작성자")
                .build();

        when(postRepository.findById(1L)).thenReturn(Optional.empty());
        // then



        UserDTO principal = new UserDTO(1L, "email", "password", "nickname", false, List.of());

        assertThrows(CustomException.class, () -> {
            commentService.createComment(commentRequestDto, principal);
        }, "해당 게시글 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("댓글 조회 단위 테스트- 중첩 구조 변환 확인")
    void findCommentListByPostId() {

        Post post = Post.builder().
                id(1L)
                .title("제목")
                .user(User.builder().email("작성자").build())
                .content("댓글 작성 테스트 ")
                .build();

        Comment parentComment = Comment.builder()
                .content("댓글 작성 테스트")
                .createdBy("작성자")
                .post(post)
                .build();

        Comment childComment= Comment.builder()
                .content("댓글 작성 테스트2222")
                .createdBy("작성자")
                .post(post)
                .parent(parentComment)
                .build();

        List<Comment> commentList = new ArrayList<>();

        commentList.add(parentComment);
        commentList.add(childComment);

        when(commentRepository.findCommentByPostId(post.getId()))
                .thenReturn(commentList);

        List<CommentResponseDto> result = commentService.findCommentListByPostId(post.getId());

        Assertions.assertThat(result.size()).isEqualTo(2);
        verify(commentRepository).findCommentByPostId(post.getId());

    }

    @Test
    @DisplayName("댓글 삭제 테스트")
    void deleteCommentTest() {

        //given
        Long commentId = 1L;
        //when
        commentService.deleteComment(commentId);
        //then
        verify(commentRepository, times(1)).deleteById(1L);
    }



}

