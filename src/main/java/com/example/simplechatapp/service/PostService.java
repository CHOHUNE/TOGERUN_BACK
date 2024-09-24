package com.example.simplechatapp.service;

import com.example.simplechatapp.dto.*;
import com.example.simplechatapp.entity.Post;
import com.example.simplechatapp.entity.PostImage;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional
public interface PostService {


    PostDTO get(Long id);

    Optional<?> findPostWithLikeAndFavorite(Long postId, Long userId);

    Long register(UserDTO principal, PostDTO postDTO, List<MultipartFile> files);


    void modify(PostDTO postDTO, List<MultipartFile> newFiles);

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
                .participateFlag(post.isParticipateFlag())
                .viewCount(post.getViewCount())
                .roadName(post.getRoadName())
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
                .imageList(post.getImageList().stream()
                        .map(PostImage::getFileName)
                        .collect(Collectors.toList()))
                .capacity(post.getCapacity())
                .activityType(post.getActivityType())
                .participateFlag(post.isParticipateFlag())
                .viewCount(post.getViewCount())
                .roadName(post.getRoadName())
                .build();

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
                .activityType(postDTO.getActivityType())
                .capacity(postDTO.getCapacity())
                .participateFlag(postDTO.isParticipateFlag())
                .viewCount(postDTO.getViewCount())
                .roadName(postDTO.getRoadName())
                .build();

        // PostImage 객체 생성 및 추가
        postDTO.getImageList().forEach(fileName -> {
            PostImage postImage = PostImage.builder().fileName(fileName).build();
            post.addImage(postImage);
        });

        return post;
    }

    Post save(PostDTO postDTO);

    List<Post> findAll();

    void incrementViewCount(Long postId, String ipAddress);
}
