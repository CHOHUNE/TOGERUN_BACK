package com.example.simplechatapp.service;

import com.example.simplechatapp.dto.*;
import com.example.simplechatapp.entity.Post;
import com.example.simplechatapp.entity.PostImage;
import com.example.simplechatapp.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
public interface PostService {


    PostDTO get(Long id);

    Optional<PostDTO> findPostWithLikeAndFavorite(Long postId, Long userId);

    Long register(UserDTO principal, PostDTO postDTO);

    void modify(PostDTO postDTO);

    PageResponseDTO<PostListDTO> getList(PageRequestDTO pageRequestDTO);

    void remove(Long id);

    default PostListDTO entityToListDTO(Post post) {
        return PostListDTO.builder().
                id(post.getId())
                .title(post.getTitle())
                .nickname(post.getUser().getNickname())
                .localDate(post.getLocalDate())
                .likeCount((long) post.getLikes().size())
                .placeName(post.getPlaceName())
                .build();

    }

    default PostDTO entityToDTO(Post post) {
        PostDTO postDTO = PostDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .localDate(post.getLocalDate())
                .nickname(post.getUser().getNickname())
                .userId(post.getUser().getId())
                .delFlag(post.isDelFlag())
                .latitude(post.getLatitude())
                .longitude(post.getLongitude())
                .placeName(post.getPlaceName())
                .meetingTime(post.getMeetingTime())
                .build();

//        List<PostImage> imageList = post.getImageList();
//
//        if (imageList == null || imageList.isEmpty()) return postDTO;
//
//        List<String> fileNameList = imageList.stream().map(postImage -> postImage.getFileName()).toList();
//
//        postDTO.setUploadFileName(fileNameList);

        return postDTO;
    }

    default Post dtoToEntity(PostDTO postDTO) {
        Post post = Post.builder()
                .id(postDTO.getId())
                .title(postDTO.getTitle())
                .content(postDTO.getContent())
                .localDate(postDTO.getLocalDate())
                .user(null) //래퍼지토리 주입 관계로 impl 에서 따로 주입
                .delFlag(postDTO.isDelFlag())
                .longitude(postDTO.getLongitude())
                .latitude(postDTO.getLatitude())
                .placeName(postDTO.getPlaceName())
                .meetingTime(postDTO.getMeetingTime())
                .build();

//        List<String> uploadFileName = postDTO.getUploadFileName();
//
//        if (uploadFileName == null || uploadFileName.isEmpty()) return post;
//
//        uploadFileName.forEach(fileName -> {
//            post.addImageString(fileName);
//        });

        return post;
    }

    Post save(PostDTO postDTO);

    List<Post> findAll();
}
