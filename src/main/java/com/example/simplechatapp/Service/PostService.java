package com.example.simplechatapp.Service;

import com.example.simplechatapp.dto.PageRequestDTO;
import com.example.simplechatapp.dto.PageResponseDTO;
import com.example.simplechatapp.dto.PostDTO;
import com.example.simplechatapp.entity.Post;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Transactional
public interface PostService {

    PostDTO get(Long id);

    Long register(PostDTO postDTO);

    void modify(PostDTO postDTO);

    PageResponseDTO<PostDTO> getList(PageRequestDTO pageRequestDTO);

    void remove(Long id);

    default PostDTO entityToDTO(Post post){
        return PostDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .localDate(post.getLocalDate())
                .user(post.getUser())
                .build();
    }

    default Post dtoToEntity(PostDTO postDTO){
        return Post.builder()
                .id(postDTO.getId())
                .title(postDTO.getTitle())
                .content(postDTO.getContent())
                .localDate(postDTO.getLocalDate())
                .user(postDTO.getUser())
                .build();
    }

    Post save(PostDTO postDTO);

    List<Post> findAll();
}
