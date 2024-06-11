package com.example.simplechatapp.repository;

import com.example.simplechatapp.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post,Long>,PostSearch {
}
