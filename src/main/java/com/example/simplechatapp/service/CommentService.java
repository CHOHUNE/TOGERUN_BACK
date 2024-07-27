package com.example.simplechatapp.service;


import com.example.simplechatapp.dto.CommentDto;
import com.example.simplechatapp.entity.Comment;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Log4j2
@RequiredArgsConstructor
public class CommentService {

    @Transactional
    public void createComment(CommentDto commentDto, HttpServletRequest request) {

    }
}
