package com.example.simplechatapp.service;

import com.example.simplechatapp.dto.LikeDTO;
import com.example.simplechatapp.entity.Like;
import com.example.simplechatapp.entity.Post;
import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.repository.LikeRepository;
import com.example.simplechatapp.repository.PostRepository;
import com.example.simplechatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    public LikeDTO likeToggle(String email, Long postId) {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));

        boolean existsByUserIdAndPostId = likeRepository.existsByUserIdAndPostId(user.getId(), postId);

        if (existsByUserIdAndPostId) {

            likeRepository.deleteByUserIdAndPostId(user.getId(), postId);

            return null; // return null if the like is removed

        } else {

            Like like = Like.builder()
                    .user(user)
                    .post(post)
                    .build();

            return convertToDTO(likeRepository.save(like));

        }

    }

    private LikeDTO convertToDTO(Like save) {
        return LikeDTO.builder()
                .id(save.getId())
                .userId(save.getUser().getId())
                .postId(save.getPost().getId())
                .build();
    }


}
