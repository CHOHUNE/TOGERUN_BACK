package com.example.simplechatapp.service;


import com.example.simplechatapp.dto.CommentRequestDto;
import com.example.simplechatapp.dto.CommentResponseDto;
import com.example.simplechatapp.entity.Comment;
import com.example.simplechatapp.entity.Post;
import com.example.simplechatapp.repository.CommentRepository;
import com.example.simplechatapp.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class CommentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public CommentResponseDto createComment(CommentRequestDto commentRequestDto) {

        Optional<Post> post = postRepository.findById(commentRequestDto.getPostId());

        Comment comment =Comment.builder()
                .post(post.orElseThrow())
                .content(commentRequestDto.getContent())
                .createdBy(commentRequestDto.getCreatedBy())
                .parent(commentRequestDto.getParentId() != null ?
                        commentRepository.findById(commentRequestDto.getParentId()).orElseThrow():null )
                .build();

        commentRepository.save(comment);
        // 게시글에는 본인도 댓글을 달 수 있기 때문에, 다른 사람이 댓글 달때에만 알림을 전송 해야 한다.
        // 해당 부분은 추후에 추가

        return CommentResponseDto.convertCommentToDto(comment);
    }

    public List<CommentResponseDto> findCommentListByPostId(Long postId) {

        List<Comment> commentLIst = commentRepository.findCommentByPostId(postId);
        // entity <-> dto

        List<CommentResponseDto> commentResponseDtoList = commentLIst.stream()
                .map(comment -> CommentResponseDto.convertCommentToDto(comment))
                .collect(Collectors.toList());
        
        return convertNestedStructure(commentResponseDtoList);

    }

    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);

    }

    @Transactional
    public void deleteCommentByPostId(Long postId) {
        // 댓글 <-> 대댓글 중첩 구조 변환
        List<CommentResponseDto> commentResponseDtoList = findCommentListByPostId(postId);

        // 부모 댓글 id 리스트 화
        List<Long> commentIdList = commentResponseDtoList.stream()
                .map(CommentResponseDto::getId)
                .collect(Collectors.toList());

        for(Long commentId : commentIdList){
          commentRepository.deleteById(commentId);
        }

    }


    /*
    *  sns 댓글 <-> 대댓글 대댓글의 중첩 구조 변환 메서드
    * */

    private List<CommentResponseDto> convertNestedStructure(List<CommentResponseDto> commentResponseDtoList) {

        List<CommentResponseDto> result = new ArrayList<>();
        Map<Long, CommentResponseDto> map = new HashMap<>();

        commentResponseDtoList.forEach(comment -> {

            map.put(comment.getId(), comment);

            if (comment.getParentId() != null) { // 댓글이 부모 댓글을 가지고 있는 경우

                map.get(comment.getParentId()).getChildren().add(comment); // 부모 댓글의 자식 리스트에 추가

            } else {

                result.add(comment); // 부모댓글이 없는 경우 ( 최상위 댓글 )

            }
        });

        return result;

    }
}