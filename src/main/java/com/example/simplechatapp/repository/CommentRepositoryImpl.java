package com.example.simplechatapp.repository;

import com.example.simplechatapp.dto.CommentDto;
import com.example.simplechatapp.entity.Comment;
import com.example.simplechatapp.entity.QComment;
import com.querydsl.core.QueryFactory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.provider.QueryComment;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CommentRepositoryImpl extends QuerydslRepositorySupport implements CommentRepository  {


    public CommentRepositoryImpl(Class<?> domainClass) {
        super(Comment.class);
    }

    private JPAQueryFactory queryFactory;



    @Override
    public List<CommentDto> convertNestedStructure(List<Comment> comments) {
        List<CommentDto> result = new ArrayList<>();
        Map<Long, CommentDto> map = new HashMap<>();

        comments.forEach(comment -> {
            CommentDto commentDto = CommentDto.convertCommentToDto(comment);
            map.put(commentDto.getId(), commentDto);
            if (comment.getParent() != null) {
                map.get(comment.getParent().getId()).getChildren().add(commentDto);
            } else {
                result.add(commentDto);
            }
        });

        return result; // 빈 리스트를 반환하는 대신 result를 반환
    }

    @Override
    public void updateComment(Comment comment) {


        queryFactory.update(QComment.comment)
                .where(QComment.comment.id.eq(comment.getId()))
                .set(QComment.comment.content, comment.getContent())
                .execute();


    }
}
