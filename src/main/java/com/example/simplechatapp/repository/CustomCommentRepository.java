package com.example.simplechatapp.repository;

import com.example.simplechatapp.entity.Comment;

import java.util.List;

public interface CustomCommentRepository {

    public List<Comment> findCommentByPostId(Long postId);
}
