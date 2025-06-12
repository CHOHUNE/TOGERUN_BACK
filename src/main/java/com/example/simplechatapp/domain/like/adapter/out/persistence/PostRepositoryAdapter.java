package com.example.simplechatapp.domain.like.adapter.out.persistence;

import com.example.simplechatapp.domain.like.application.port.out.PostPort;
import com.example.simplechatapp.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PostRepositoryAdapter implements PostPort {

    private final PostRepository postRepository;


    @Override
    public boolean existById(Long postId) {
        return postRepository.existsById(postId);
    }

    @Override
    public Optional<Long> findAuthorIdByPostId(Long postId) {
        return postRepository.findById(postId).map(post -> post.getUser().getId());
    }
}
