package com.example.simplechatapp.repository;

import com.example.simplechatapp.dto.PageRequestDTO;
import com.example.simplechatapp.dto.PageResponseDTO;
import com.example.simplechatapp.dto.PostDTO;
import com.example.simplechatapp.entity.Post;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface PostSearch {

    Page<Post> search1(PageRequestDTO pageRequestDTO);

    Optional<PostDTO> findPostWithLikeAndFavorite(Long postId, Long userId);

}
