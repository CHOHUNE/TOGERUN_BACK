package com.example.simplechatapp.Service;


import com.example.simplechatapp.entity.ChatRoom;
import com.example.simplechatapp.entity.Post;
import com.example.simplechatapp.repository.ChatRoomRepository;
import com.example.simplechatapp.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public List<Post> findAll() {
        return postRepository.findAll();

    }

    public Optional<Post> findById(Long id) {


        return postRepository.findById(id);
    }


    public Post save(Post post) {
        return postRepository.save(post);
    }
}
