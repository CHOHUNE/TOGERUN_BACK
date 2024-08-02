package com.example.simplechatapp.service;


import com.example.simplechatapp.dto.PageRequestDTO;
import com.example.simplechatapp.dto.PageResponseDTO;
import com.example.simplechatapp.dto.PostDTO;
import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.entity.Post;
import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.repository.PostRepository;
import com.example.simplechatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    public PostDTO get(Long id) {

        Optional<Post> result = postRepository.findById(id);
        Post post = result.orElseThrow();

        return entityToDTO(post);
    }


    @Override
        public Long register( UserDTO principal, PostDTO postDTO) {

        User user = userRepository.findByEmail(principal.getEmail());

        postDTO.setLocalDate(LocalDate.now());
        postDTO.setUserId(user.getId());

        Post result = postRepository.save(dtoToEntity(postDTO));

        return result.getId();
    }

    @Override
    public void modify(PostDTO postDTO) {

        Optional<Post> result = postRepository.findById(postDTO.getId());

        Post post = result.orElseThrow();

        post.changeTitle(postDTO.getTitle());
        post.changeContent(postDTO.getContent());

        List<String> uploadFileNames = postDTO.getUploadFileName();

        post.clearList();

        if (uploadFileNames != null && !uploadFileNames.isEmpty()) {

            uploadFileNames.forEach(uploadFileName ->{
                post.addImageString(uploadFileName);
            });
        }

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
//        postRepository.updateToDelete(id,true);

    }


    @Override
    public PostDTO entityToDTO(Post post) {
        return PostService.super.entityToDTO(post);
    }

    @Override
    public Post dtoToEntity(PostDTO postDTO) {

        Post post = PostService.super.dtoToEntity(postDTO);

        post.setUser(userRepository.findById(postDTO.getUserId()));

        return post;
    }

    @Override
    public Post save(PostDTO postDTO) {
        return postRepository.save(dtoToEntity(postDTO));
    }

    @Override
    public List<Post> findAll() {
        return postRepository.findAll();
    }
}
