package com.example.simplechatapp.Service;

import com.example.simplechatapp.dto.PageRequestDTO;
import com.example.simplechatapp.dto.PageResponseDTO;
import com.example.simplechatapp.dto.PostDTO;
import com.example.simplechatapp.entity.Post;
import com.example.simplechatapp.entity.PostImage;
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
        PostDTO postDTO = PostDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .localDate(post.getLocalDate())
                .user(post.getUser())
                .delFlag(post.isDelFlag())
                .build();

        List<PostImage> imageList = post.getImageList();

        if(imageList == null || imageList.isEmpty()) return postDTO;

        List<String> fileNameList = imageList.stream().map(postImage -> postImage.getFileName()).toList();

        postDTO.setUploadFileName(fileNameList);

        return postDTO;
    }

    default Post dtoToEntity(PostDTO postDTO){
        Post post =  Post.builder()
                .id(postDTO.getId())
                .title(postDTO.getTitle())
                .content(postDTO.getContent())
                .localDate(postDTO.getLocalDate())
                .user(postDTO.getUser())
                .delFlag(postDTO.isDelFlag())
                .build();

        List<String> uploadFileName = postDTO.getUploadFileName();

        if( uploadFileName == null || uploadFileName.isEmpty()) return post;

        uploadFileName.forEach(fileName -> {
            post.addImageString(fileName);
        });

        return post;
    }

    Post save(PostDTO postDTO);

    List<Post> findAll();
}
