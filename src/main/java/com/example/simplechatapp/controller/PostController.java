package com.example.simplechatapp.controller;


import com.example.simplechatapp.Service.PostService;
import com.example.simplechatapp.dto.PageRequestDTO;
import com.example.simplechatapp.dto.PageResponseDTO;
import com.example.simplechatapp.dto.PostDTO;
import com.example.simplechatapp.entity.Post;
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

    @GetMapping
    public List<Post> getAllPosts() {
        return postService.findAll();
    }

    @GetMapping("/{id}")
    public PostDTO getPostById(@PathVariable Long id) {

        return postService.get(id);
    }

    @PostMapping
    public Map<String,Long> createPost( PostDTO postDTO) {

         postService.save(postDTO);

        log.info("postDTO{}", postDTO.getId());

        return Map.of("id", postDTO.getId());
    }

    @GetMapping("/list")
    public PageResponseDTO<PostDTO> list(PageRequestDTO pageRequestDTO) {

        log.info("list:{}", pageRequestDTO);

        return postService.getList(pageRequestDTO);
    }

    @PutMapping("/{id}")
    public Map<String, String> modify (@PathVariable Long id, @RequestBody PostDTO postDTO) {
        postDTO.setId(id);
        postService.modify(postDTO);

        return Map.of("result","success");
    }

    @DeleteMapping("/{id}")
    public Map<String, String> remove(@PathVariable Long id) {
        postService.remove(id);

        return Map.of("result","success");

    }
}
