package com.example.simplechatapp.repository;

import com.example.simplechatapp.entity.Post;
import org.springframework.data.domain.Page;

public interface PostSearch {

    Page<Post> search1();
}
