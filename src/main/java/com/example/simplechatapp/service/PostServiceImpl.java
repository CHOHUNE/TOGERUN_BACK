package com.example.simplechatapp.service;


import com.example.simplechatapp.dto.*;
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
    public Optional<PostDTO> findPostWithLikeAndFavorite(Long postId, Long userId) {
        return postRepository.findPostWithLikeAndFavorite(postId, userId);
    }


    @Override
        public Long register( UserDTO principal, PostDTO postDTO) {

        User user = userRepository.findByEmail(principal.getEmail()).orElseThrow(()->new RuntimeException("User Not Found"));

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
        post.changeMeetingTime(postDTO.getMeetingTime());
        post.changePlaceName(postDTO.getPlaceName());
        post.changeLatitude(postDTO.getLatitude());
        post.changeLongitude(postDTO.getLongitude());

        // 고민.. 차라리 Builder 로 만들어서 변경하면?
        // 코드 유지보수, 확장성은 좋으나 비효율적
        // 현재의 change 는? 코드가 복잡해지나 성능은 나을 수 있다.

//        List<String> uploadFileNames = postDTO.getUploadFileName();
//
//        post.clearList();
//
//        if (uploadFileNames != null && !uploadFileNames.isEmpty()) {
//
//            uploadFileNames.forEach(uploadFileName ->{
//                post.addImageString(uploadFileName);
//            });
//        }

        postRepository.save(post);
    }

    @Override
    public PageResponseDTO<PostListDTO> getList(PageRequestDTO pageRequestDTO) {
        Page<Post> result = postRepository.search1(pageRequestDTO);
        List<PostListDTO> dtoList = result.getContent().stream()
                .map(this::entityToListDTO)
                .collect(Collectors.toList());

        return PageResponseDTO.<PostListDTO>withAll()
                .dtoList(dtoList)
                .pageRequestDTO(pageRequestDTO)
                .total(result.getTotalElements())
                .build();
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

        post.setUser(userRepository.findById(postDTO.getUserId()).orElseThrow(() -> new RuntimeException("User not found")));

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
