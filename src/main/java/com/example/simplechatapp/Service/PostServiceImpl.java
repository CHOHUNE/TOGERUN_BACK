package com.example.simplechatapp.Service;


import com.example.simplechatapp.dto.PageRequestDTO;
import com.example.simplechatapp.dto.PageResponseDTO;
import com.example.simplechatapp.dto.PostDTO;
import com.example.simplechatapp.entity.Post;
import com.example.simplechatapp.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public PageResponseDTO<PostDTO> getList(PageRequestDTO pageRequestDTO) {

        Page<Post> result = postRepository.search1(pageRequestDTO);

        List<PostDTO> dtoList = result
                .get()
                .map(post -> entityToDTO(post)).collect(Collectors.toList());
        // PageRequestDTO -> Post(Entity) -> PostDTO(List)

        PageResponseDTO<PostDTO> responseDTO =
                PageResponseDTO.<PostDTO>withAll()
                        .dtoList(dtoList)
                        .pageRequestDTO(pageRequestDTO)
                        .total(result.getTotalElements())
                        .build();
        // dtoList(PostDTO) , PageRequestDTO,
        // total 값을 가지고 PageResponseDTO  생성

        return responseDTO;
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
