package com.example.simplechatapp.service;


import com.example.simplechatapp.dto.CommentRequestDto;
import com.example.simplechatapp.dto.CommentResponseDto;
import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.entity.Comment;
import com.example.simplechatapp.entity.Post;
import com.example.simplechatapp.repository.CommentRepository;
import com.example.simplechatapp.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.cache.RedisCacheManager;
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
    private final RedisCacheManager cacheManager;

    @CacheEvict(value="postComments", key="#commentRequestDto.post_id")
    @Transactional
    public CommentResponseDto createComment(CommentRequestDto commentRequestDto, UserDTO principal) {

        Optional<Post> post = postRepository.findById(commentRequestDto.getPost_id());

        Comment comment = Comment.builder()
                .post(post.orElseThrow())
                .content(commentRequestDto.getContent())
                .createdBy(principal.getEmail()) //
                .nickName(principal.getNickname())
                .img(principal.getImg())
                .parent(commentRequestDto.getParent_id() != null ?
                        commentRepository.findById(commentRequestDto.getParent_id()).orElseThrow() : null)
                .build();

        commentRepository.save(comment);
        // 게시글에는 본인도 댓글을 달 수 있기 때문에, 다른 사람이 댓글 달때에만 알림을 전송 해야 한다.
        // 해당 부분은 추후에 추가

        return CommentResponseDto.convertCommentToDto(comment);
    }

    @CacheEvict(value="postComments", key="#commentRequestDto.post_id")
    public CommentResponseDto modifyComment(CommentRequestDto commentRequestDto,UserDTO principal) {

        //댓글 조회 : RequestDto 에 있는 id 로 조회
        Comment comment = commentRepository.findById(commentRequestDto.getId()).orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        //댓글 작성자와 수정 요청자가 같은지 확인
        if (!principal.getEmail().equals(comment.getCreatedBy())) {
            throw new IllegalArgumentException("댓글 작성자만 수정 가능합니다.");
        }


        //업데이트
        comment.update(commentRequestDto.getContent());
        //저장
        commentRepository.save(comment);

        return CommentResponseDto.convertCommentToDto(comment);
    }


    @Cacheable(value = "postComments", key = "#postId")
    public List<CommentResponseDto> findCommentListByPostId(Long postId) {

        List<Comment> commentLIst = commentRepository.findCommentByPostId(postId);
        // entity <-> dto

        List<CommentResponseDto> commentResponseDtoList = commentLIst.stream()
                .map(CommentResponseDto::convertCommentToDto)
                .collect(Collectors.toList());

        return convertNestedStructure(commentResponseDtoList);
    }


//    @CacheEvict(value="postComments",key="#result")
    public Long deleteComment(Long commentId) {

        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("Comment NOT FOUND"));


        comment.updateDelFlag(!comment.isDelFlag());
        commentRepository.save(comment);
        cacheManager.getCache("postComments").evict(comment.getPost().getId());

         return commentId;
    }


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
