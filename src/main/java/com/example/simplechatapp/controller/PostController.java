package com.example.simplechatapp.controller;


import com.example.simplechatapp.service.PostService;
import com.example.simplechatapp.dto.PageRequestDTO;
import com.example.simplechatapp.dto.PageResponseDTO;
import com.example.simplechatapp.dto.PostDTO;
import com.example.simplechatapp.entity.Post;
import com.example.simplechatapp.util.CustomFileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Log4j2
public class PostController {

    private final PostService postService;
    private final CustomFileUtil customFileUtil;

    @GetMapping
    public List<Post> getAllPosts() {
        return postService.findAll();
    }

    @GetMapping("/{id}")
    public PostDTO getPostById(@PathVariable Long id) {

        return postService.get(id);
    }

//    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    @PostMapping
    public Map<String,Long> createPost( PostDTO postDTO) {

        // MultiPart-Data-Form 양식으로 보낼 경우 : @RequestBody 사용하지 않음
        // @RequestBody 사용시 JSON 형태로 보내야함

        log.info("postDTO{}", postDTO);

        Long id = postService.register(postDTO);

        return Map.of("id", id);
    }

    @GetMapping("/list")
    public PageResponseDTO<PostDTO> list(PageRequestDTO pageRequestDTO) {

        log.info("list:{}", pageRequestDTO);

        return postService.getList(pageRequestDTO);
    }

    @PutMapping("/{id}")
    public Map<String, String> modify (@PathVariable Long id,  PostDTO postDTO) {
        postDTO.setId(id);
        postService.modify(postDTO);

        return Map.of("result","success");
    }

    @DeleteMapping("/{id}")
    public Map<String, String> remove(@PathVariable Long id) {

        List<String> oldFileNames = postService.get(id).getUploadFileName();
        customFileUtil.deleteFile(oldFileNames);
        postService.remove(id);

        return Map.of("result","success");
    }
}
