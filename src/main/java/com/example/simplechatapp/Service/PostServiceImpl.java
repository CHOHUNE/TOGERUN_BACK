package com.example.simplechatapp.Service;


import com.example.simplechatapp.dto.PostDTO;
import com.example.simplechatapp.entity.Post;
import com.example.simplechatapp.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    public List<Post> findAll() {
        return postRepository.findAll();

    }

    @Override
    public PostDTO get(Long id) {

        Optional<Post> result = postRepository.findById(id);
        Post post = result.orElseThrow();

        return entityToDTO(post);
    }

    @Override
    public Long register(PostDTO postDTO) {

        Post save = postRepository.save(dtoToEntity(postDTO));

        return postDTO.getId();
    }

    @Override
    public void modify(PostDTO postDTO) {

        Optional<Post> result = postRepository.findById(postDTO.getId());

        Post post = result.orElseThrow();

        post.changeTitle(postDTO.getTitle());
        post.changeContent(postDTO.getContent());


        postRepository.save(post);
    }

    @Override
    public void remove(Long id) {

        postRepository.deleteById(id);

    }


    public Post save(Post post) {
        return postRepository.save(post);
    }

    @Override
    public PostDTO entityToDTO(Post post) {
        return PostService.super.entityToDTO(post);
    }

    @Override
    public Post dtoToEntity(PostDTO postDTO) {
        return PostService.super.dtoToEntity(postDTO);
    }
}
