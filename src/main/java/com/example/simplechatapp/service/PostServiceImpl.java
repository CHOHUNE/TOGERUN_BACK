package com.example.simplechatapp.service;

import com.example.simplechatapp.dto.*;
import com.example.simplechatapp.entity.Post;
import com.example.simplechatapp.entity.PostImage;
import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.repository.PostRepository;
import com.example.simplechatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final S3Client s3Client;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("${image.file.prefix}")
    private String urlPrefix;

    @Override
    public PostDTO get(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return entityToDTO(post);
    }

    @Override
    public Optional<PostDTO> findPostWithLikeAndFavorite(Long postId, Long userId) {
        return postRepository.findPostWithLikeAndFavorite(postId, userId);
    }

    @Override
    @Transactional
    public Long register(UserDTO principal, PostDTO postDTO, List<MultipartFile> files) {
        User user = userRepository.findByEmail(principal.getEmail())
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        postDTO.setLocalDate(LocalDate.now());
        postDTO.setUserId(user.getId());

        if (files != null && !files.isEmpty()) {
            List<String> uploadFileUrls = uploadFiles(files);
            postDTO.setImageList(uploadFileUrls);
        }

        Post post = dtoToEntity(postDTO);
        Post savedPost = postRepository.save(post);

        return savedPost.getId();
    }

    @Override
    @Transactional
    public void modify(PostDTO postDTO, List<MultipartFile> newFiles) {
        Post post = postRepository.findById(postDTO.getId())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        updatePostFields(post, postDTO);

        if (newFiles != null && !newFiles.isEmpty()) {
            deleteFiles(post.getImageList());
            List<String> newFileUrls = uploadFiles(newFiles);
            post.clearList();
            newFileUrls.forEach(post::addImageString);
        }

        postRepository.save(post);
    }

    @Override
    @Transactional
    public void remove(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        deleteFiles(post.getImageList());
        postRepository.deleteById(id);
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
    public Post dtoToEntity(PostDTO postDTO) {
        Post post = PostService.super.dtoToEntity(postDTO);
        User user = userRepository.findById(postDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        post.setUser(user);
        return post;
    }

    @Override
    @Transactional
    public Post save(PostDTO postDTO) {
        return postRepository.save(dtoToEntity(postDTO));
    }

    @Override
    public List<Post> findAll() {
        return postRepository.findAll();
    }

    private void updatePostFields(Post post, PostDTO postDTO) {
        post.changeTitle(postDTO.getTitle());
        post.changeContent(postDTO.getContent());
        post.changeMeetingTime(postDTO.getMeetingTime());
        post.changePlaceName(postDTO.getPlaceName());
        post.changeLatitude(postDTO.getLatitude());
        post.changeLongitude(postDTO.getLongitude());
    }

    private String convertFileName(String originalFileName) {
        return UUID.randomUUID().toString() + "_" + originalFileName;
    }

    private void deleteFiles(List<PostImage> postImages) {
        for (PostImage postImage : postImages) {
            String fileUrl = postImage.getFileName();
            String fileName = fileUrl.substring(urlPrefix.length());
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        }
    }

    private List<String> uploadFiles(List<MultipartFile> files) {
        List<CompletableFuture<String>> futures = new ArrayList<>();

        for (MultipartFile file : files) {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                String fileName = convertFileName(file.getOriginalFilename());
                try {
                    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .build();

                    s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

                    return urlPrefix + fileName;
                } catch (IOException e) {
                    throw new RuntimeException("Failed to Upload file", e);
                }
            });

            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
    }
}