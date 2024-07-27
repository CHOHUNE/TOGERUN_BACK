package com.example.simplechatapp.repository;

import com.example.simplechatapp.dto.CommentDto;
import com.example.simplechatapp.entity.Comment;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface CommentRepository {

    public List<CommentDto> convertNestedStructure(List<Comment> comments);

    public void updateComment(Comment comment);

}
