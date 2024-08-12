package com.example.simplechatapp.service;

import com.example.simplechatapp.dto.LikeDTO;
import com.example.simplechatapp.entity.Like;
import com.example.simplechatapp.entity.Post;
import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.repository.LikeRepository;
import com.example.simplechatapp.repository.PostRepository;
import com.example.simplechatapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;


    @Transactional // 여러 사람이 좋아요를 누를 때를 대비해 동시성 이슈를 피하기 위해 트랜잭션 처리를 한다.
    public LikeDTO likeToggle(String email, Long postId) {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post not found"));

        boolean isFirstLike = false;

        Like like = likeRepository.findByUserIdAndPostId(user.getId(), post.getId()).
                orElse(null);




        if (like == null) {

            like = Like.builder()
                    .user(user)
                    .post(post)
                    .isActive(true)
                    .build();

            isFirstLike = true;
        }else{

            like.setActive(!like.isActive());
        }


        Like saveLike = likeRepository.save(like);


        return LikeDTO.convertLikeToDto(saveLike, isFirstLike && like.isActive());

    }




}
