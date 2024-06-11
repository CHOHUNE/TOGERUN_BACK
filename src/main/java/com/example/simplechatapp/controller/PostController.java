package com.example.simplechatapp.controller;


import com.example.simplechatapp.Service.PostService;
import com.example.simplechatapp.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public List<Post> getAllPosts() {
        return postService.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Post> getPostById(@PathVariable Long id) {

        return postService.findById(id);
    }

    @PostMapping
    public Post createPost(@RequestBody Post post) {


        return postService.save(post);
    }


}
