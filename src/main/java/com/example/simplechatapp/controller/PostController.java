package com.example.simplechatapp.controller;


import com.example.simplechatapp.Service.PostServiceImpl;
import com.example.simplechatapp.dto.PostDTO;
import com.example.simplechatapp.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostServiceImpl postServiceImpl;

    @GetMapping
    public List<Post> getAllPosts() {
        return postServiceImpl.findAll();
    }

    @GetMapping("/{id}")
    public PostDTO getPostById(@PathVariable Long id) {

        return postServiceImpl.get(id);
    }

    @PostMapping
    public Post createPost(@RequestBody Post post) {


        return postServiceImpl.save(post);
    }


}
