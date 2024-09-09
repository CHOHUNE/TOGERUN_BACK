package com.example.simplechatapp.service;

import com.example.simplechatapp.dto.FavoriteDTO;
import com.example.simplechatapp.entity.Favorite;
import com.example.simplechatapp.entity.Post;
import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.repository.FavoriteRepository;
import com.example.simplechatapp.repository.PostRepository;
import com.example.simplechatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @CacheEvict(value = "post", key = "#postId")
    public FavoriteDTO favoriteToggle(String email, Long postId) {

        User user = userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("User not found"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));

        Favorite favorite  = favoriteRepository.findByUserIdAndPostId(user.getId(), post.getId())
                .orElse(
                        Favorite.builder()
                                .user(user)
                                .post(post)
                                .isActive(false)
                                .build()
                );


        favorite.setActive(!favorite.isActive());

        return convertToDTO(favoriteRepository.save(favorite));
    }

    private FavoriteDTO convertToDTO(Favorite save) {
        return FavoriteDTO.builder()
                .id(save.getId())
                .userId(save.getUser().getId())
                .postId(save.getPost().getId())
                .build();
    }
}
