package com.example.simplechatapp.controller;


import com.example.simplechatapp.annotation.NeedNotify;
import com.example.simplechatapp.dto.*;
import com.example.simplechatapp.entity.Post;
import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.repository.UserRepository;
import com.example.simplechatapp.service.FavoriteService;
import com.example.simplechatapp.service.LikeService;
import com.example.simplechatapp.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
@Log4j2
@PreAuthorize("hasRole('ROLE_SILVER')") //SpringSecurity 5.0 이후에서는 ROLE_ 접두사를 사용하지 않아도 무방 ( 오히려 생략이 권장 )
public class PostController {

    private final PostService postService;
    private final LikeService likeService;
    private final FavoriteService favoriteService;
    private final UserRepository userRepository;


    @GetMapping
    public List<Post> getAllPosts() {

        return postService.findAll();
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable Long id, @AuthenticationPrincipal UserDTO principal, HttpServletRequest request) {

        User user = userRepository.findByEmail(principal.getEmail())
                .orElseThrow(() -> new RuntimeException("User Not Found"));


        postService.incrementViewCount(id, principal.getId());

        return postService.findPostWithLikeAndFavorite(id, user.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    //    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    @PostMapping
    public Map<String, Long> createPost(
            @AuthenticationPrincipal UserDTO principal,
            @RequestParam(value = "uploadFiles", required = false) List<MultipartFile> files,
            PostDTO postDTO) {

        // MultiPart-Data-Form 양식으로 보낼 경우 : @RequestBody 사용하지 않음
        // @RequestBody 사용시 JSON 형태로 보내야함

        log.info("postDTO{}", postDTO);

        Long id = postService.register(principal, postDTO, files);


        return Map.of("id", id);
    }

    //    @Cacheable(value = "postList", key = "#pageRequestDTO.toString()")
    @GetMapping("/list")
    public PageResponseDTO<PostListDTO> list(PageRequestDTO pageRequestDTO) {

        log.info("list:{}", pageRequestDTO);

        return postService.getList(pageRequestDTO);
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> modify(
            @PathVariable Long id, PostDTO postDTO,
            @RequestParam(value = "uploadFiles", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal UserDTO principal) {

        postService.checkAuthorization(id, principal.getEmail());
        postService.modify(postDTO, files);
        postDTO.setId(id);
        return ResponseEntity.ok(Map.of("result", "success"));

    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> remove(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDTO principal) {

        postService.checkAuthorization(id, principal.getEmail());

        List<String> oldFileNames = postService.get(id).getImageList();

        postService.remove(id);

        return ResponseEntity.ok(Map.of("result", "success", "oldFileNames", oldFileNames));
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<FavoriteDTO> toggleFavorite(@PathVariable Long id, @AuthenticationPrincipal UserDTO principal) {


        log.info("toggleFavoriteEmail:{}", principal.getEmail());

        FavoriteDTO favoriteDTO = favoriteService.favoriteToggle(principal.getEmail(), id);
        return ResponseEntity.ok(favoriteDTO);
    }

    @PostMapping("/{id}/like")
    @NeedNotify
    public ResponseEntity<LikeDTO> toggleLike(@PathVariable Long id, @AuthenticationPrincipal UserDTO principal) {

        LikeDTO likeDTO = likeService.likeToggle(principal.getEmail(), id);

        return ResponseEntity.ok(likeDTO);
    }


}
