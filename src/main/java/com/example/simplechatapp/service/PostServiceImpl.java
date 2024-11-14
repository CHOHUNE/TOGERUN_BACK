package com.example.simplechatapp.service;

import com.example.simplechatapp.dto.*;
import com.example.simplechatapp.entity.Post;
import com.example.simplechatapp.entity.PostImage;
import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.entity.UserRole;
import com.example.simplechatapp.repository.PostRepository;
import com.example.simplechatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final StringRedisTemplate stringRedisTemplate;

    private static final String VIEW_COUNT_PREFIX = "view:";
    private static final Duration VIEW_COUNT_EXPIRATION = Duration.ofHours(24);

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("${image.file.prefix}")
    private String urlPrefix;

    @Override
    public PostDTO get(Long id) {

        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        PostDTO dto = entityToDTO(post);
        dto.setImageList(post.getImageList().stream()
                .map(image -> addPrefix(image.getFileName()))
                .collect(Collectors.toList()));
        dto.setExistingImageUrls(new ArrayList<>(dto.getImageList()));
        return dto;
    }

    @Override
    @Cacheable(value = "post", key = "#postId")
    public Optional<?> findPostWithLikeAndFavorite(Long postId, Long userId) {
        return postRepository.findPostWithLikeAndFavorite(postId, userId);
    }

    @Async
    @Override
    public void incrementViewCount(Long postId, String ipAddress) {
        String key = VIEW_COUNT_PREFIX + postId + ":" + ipAddress;
        Boolean keyAbsent = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", VIEW_COUNT_EXPIRATION);

        if (Boolean.TRUE.equals(keyAbsent)) {
            postRepository.incrementViewCount(postId);
        }
    }

    @Override
    public void checkAuthorization(Long postId, String userEmail)  {

        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        User user = userRepository.findByEmail(userEmail).orElseThrow(()-> new RuntimeException("User not found"));

        boolean isAuthor = post.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getUserRoleList().contains(UserRole.ROLE_ADMIN);

        if (!isAuthor && !isAdmin) {
            throw new AccessDeniedException("게시물의 작성자나 관리자 계정이 아닙니다.");
        }
    }

    @Override
    @Transactional
    public Long register(UserDTO principal, PostDTO postDTO, List<MultipartFile> files) {
        User user = userRepository.findByEmail(principal.getEmail()).orElseThrow(() -> new RuntimeException("User Not Found"));

        postDTO.setLocalDate(LocalDate.now());
        postDTO.setUserId(user.getId());

        Post post = dtoToEntity(postDTO);
        Post savedPost = postRepository.save(post);

        if (files != null && !files.isEmpty()) {
            List<String> uploadFileUrls = uploadFiles(files, savedPost.getId());
            uploadFileUrls.forEach(url -> {
                savedPost.addImageString(removePrefix(url));
                postDTO.getImageList().add(url);
            });
            postDTO.setExistingImageUrls(new ArrayList<>(postDTO.getImageList()));
        }

        return savedPost.getId();
    }

    @Override
    @CacheEvict(value = {"post"}, key = "#postDTO.id")
    @Transactional
    public void modify(PostDTO postDTO, List<MultipartFile> newFiles) {
        Post post = postRepository.findById(postDTO.getId()).orElseThrow(() -> new RuntimeException("POST NOT FOUND"));

        updatePostFields(post, postDTO);
        updatePostImages(post, postDTO);

        if (newFiles != null && !newFiles.isEmpty()) {
            List<String> newImageUrls = uploadFiles(newFiles, post.getId());
            newImageUrls.forEach(url -> {
                post.addImageString(removePrefix(url));
                postDTO.getImageList().add(url);
            });
        }

        postDTO.setExistingImageUrls(new ArrayList<>(postDTO.getImageList().stream()
                .map(this::addPrefix)
                .collect(Collectors.toList())));
        postRepository.save(post);
    }

    @Override
    @Caching(evict = {@CacheEvict(value = "post", key = "#id"), @CacheEvict(value = "postComments", key = "#id")})
    @Transactional
    public void remove(Long id) {

        Post post = postRepository.findById(id).orElseThrow(()-> new RuntimeException("Post not found"));
        deleteFiles(post.getImageList());
        postRepository.deleteById(id);

    }

    @Override
    public PageResponseDTO<PostListDTO> getList(PageRequestDTO pageRequestDTO) {
        Page<Post> result = postRepository.search1(pageRequestDTO);
        List<PostListDTO> dtoList = result.getContent().stream().map(this::entityToListDTO).collect(Collectors.toList());

        return PageResponseDTO.<PostListDTO>withAll()
                .dtoList(dtoList)
                .pageRequestDTO(pageRequestDTO)
                .total(result.getTotalElements())
                .build();
    }

    @Override
    public Post dtoToEntity(PostDTO postDTO) {
        Post post = PostService.super.dtoToEntity(postDTO);
        User user = userRepository.findById(postDTO.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));
        post.changeUser(user);
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
        post.changeRoadName(postDTO.getRoadName());
        post.changeActivityType(postDTO.getActivityType());
        post.changeCapacity(postDTO.getCapacity());
        post.changeParticipateFlag(postDTO.isParticipateFlag());
    }

    private void updatePostImages(Post post, PostDTO postDTO) {
        List<PostImage> currentImages = post.getImageList();

        List<PostImage> imagesToDelete = currentImages.stream()
                .filter(image -> !postDTO.getExistingImageUrls().contains(addPrefix(image.getFileName())))
                .collect(Collectors.toList());

        deleteFiles(imagesToDelete);

        post.clearList();
        postDTO.getExistingImageUrls().forEach(url -> post.addImageString(removePrefix(url)));
    }

    private void deleteFiles(List<PostImage> postImages) {
        for (PostImage postImage : postImages) {
            String fileName = postImage.getFileName();
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        }
    }

    private List<String> uploadFiles(List<MultipartFile> files, Long postId) {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }

        List<CompletableFuture<String>> futures = new ArrayList<>();

        for (MultipartFile file : files) {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                String originalFileName = file.getOriginalFilename();
                String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
                String fileName = UUID.randomUUID().toString() + fileExtension;
                String fileKey = String.format("chatApp/post/%d/%s", postId, fileName);

                try {
                    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileKey)
                            .build();

                    s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

                    return addPrefix(fileKey);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to Upload file", e);
                }
            });

            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
    }

    private String removePrefix(String url) {
        return url.startsWith(urlPrefix) ? url.substring(urlPrefix.length()) : url;
    }

    private String addPrefix(String key) {
        return key.startsWith(urlPrefix) ? key : urlPrefix + key;
    }

    @Override
    @Scheduled(cron = "0 0 0/15 * * *")
    public void updatedParticipateFlag() {
        LocalDateTime now = LocalDateTime.now();
        List<Post> posts = postRepository.findByMeetingTimeBefore(now);

        for (Post post : posts) {
            post.changeParticipateFlag(false);
            postRepository.save(post);
        }
    }
}