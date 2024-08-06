package com.example.simplechatapp.service;

import com.example.simplechatapp.dto.FavoriteDTO;
import com.example.simplechatapp.entity.Favorite;
import com.example.simplechatapp.entity.Post;
import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.repository.FavoriteRepository;
import com.example.simplechatapp.repository.PostRepository;
import com.example.simplechatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public FavoriteDTO favoriteToggle(Long userId, Long postId) {
        User user = userRepository.findById(userId);
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));

        boolean existsByUserIdAndPostId = favoriteRepository.existsByUserIdAndPostId(userId, postId);

        if (existsByUserIdAndPostId) {

            favoriteRepository.deleteByUserIdAndPostId(userId, postId);

            return null; // return null if the favorite is removed

        } else {
            Favorite favorite = Favorite.builder()
                    .user(user)
                    .post(post)
                    .build();

            return convertToDTO(favoriteRepository.save(favorite));


        }


    }

    private FavoriteDTO convertToDTO(Favorite save) {
        return FavoriteDTO.builder()
                .id(save.getId())
                .userId(save.getUser().getId())
                .postId(save.getPost().getId())
                .build();
    }
}
