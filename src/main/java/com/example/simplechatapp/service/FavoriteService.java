package com.example.simplechatapp.service;

import com.example.simplechatapp.dto.FavoriteDTO;
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

//    public FavoriteDTO addFavorite(Long userId, Long postId){
//        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
//        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
//
//    }
}
