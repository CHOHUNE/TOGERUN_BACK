package com.example.simplechatapp.service;

import com.example.simplechatapp.dto.*;
import com.example.simplechatapp.entity.Post;
import com.example.simplechatapp.entity.PostImage;
import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.entity.UserRole;
import com.example.simplechatapp.repository.PostRepository;
import com.example.simplechatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
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
@Log4j2
public class PostServiceImpl implements PostService {

    private final S3Client s3Client;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String VIEW_COUNT_PREFIX = "post:view:";
    private static final Duration VIEW_COUNT_EXPIRATION = Duration.ofHours(24);
    private final RedisTemplate redisTemplate;

    private static final int BATCH_SIZE=100;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("${image.file.prefix}")
    private String urlPrefix;

    @Override
    public PostDTO get(Long id) {

        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        PostDTO dto = entityToDTO(post);

        // Redis의 임시 조회수도 확인
        String key = VIEW_COUNT_PREFIX + id;
        Long redisCount = stringRedisTemplate.opsForZSet().size(key);
        if (redisCount != null) {
            dto.setViewCount(post.getViewCount() + redisCount);
        }

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
    public void incrementViewCount(Long postId, Long userId) {
        if (userId == null) {
            return; // 비로그인 사용자는 카운트 하지 않음
        }
        String key = VIEW_COUNT_PREFIX + postId;
        ZSetOperations<String, String> zSetOps = stringRedisTemplate.opsForZSet();
        // ZSetOperations : Redis의 Sorted Set을 다루는 인터페이스

        double currentTime = System.currentTimeMillis();

        zSetOps.removeRangeByScore(key,0,currentTime - VIEW_COUNT_EXPIRATION.toMillis());
        Boolean added = zSetOps.add(key,userId.toString(),currentTime);
        // ZSet에 userId를 추가하고, 이미 존재하면 false를 반환
        // 각 파라메터 값 : key, value, score:시간

        if (Boolean.TRUE.equals(added)) {
            redisTemplate.expire(key, VIEW_COUNT_EXPIRATION);

            postRepository.incrementViewCount(postId,1L);
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
    @Scheduled(cron = "0 */15 * * * *")  // 15분마다 실행
    public void updatedParticipateFlag() {
        log.info("참가 여부 플래그 업데이트시작: {}", LocalDateTime.now());

        LocalDateTime now = LocalDateTime.now();

        try {
            int updateCount = postRepository.bulkUpdateParticipateFlag(now);
            log.info("참가 플래그 업데이트 완료, 업데이트 게시물 수 :{}", updateCount);
        } catch (Exception e) {
            log.error("참가 플래그 업데이트 중 오류 발생", e);
        }
    }

}