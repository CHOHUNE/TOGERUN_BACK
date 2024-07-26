package com.example.simplechatapp.repository;

import com.example.simplechatapp.dto.PageRequestDTO;
import com.example.simplechatapp.entity.Post;
import com.example.simplechatapp.entity.QPost;
import com.example.simplechatapp.entity.QUser;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class PostSearchImpl extends QuerydslRepositorySupport implements PostSearch {

    public PostSearchImpl() {
        super(Post.class);
    }

    @Override
    public Page<Post> search1(PageRequestDTO pageRequestDTO) {
        QPost qPost = QPost.post;
        QUser qUser = QUser.user;
        // List

        JPQLQuery<Post> query = from(qPost)
                .leftJoin(qPost.user, qUser).fetchJoin()
//                .leftJoin(qPost.imageList).fetchJoin() ElementCollection 은 fetchJoin 이 안된다.
                .where(
                        containsKeyword(qPost, pageRequestDTO.getKeyword()),
                        qPost.delFlag.eq(false)
                )
                .distinct();

        Pageable pageable = PageRequest.of(
                pageRequestDTO.getPage() - 1,
                pageRequestDTO.getSize(),
                Sort.by("id").descending()
        );

        List<Post> list = getQuerydsl().applyPagination(pageable, query).fetch();

        long total = query.fetchCount();

        return new PageImpl<>(list, pageable, total);
    }

    private BooleanExpression containsKeyword(QPost qPost, String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return null;
        }
        return qPost.title.containsIgnoreCase(keyword)
                .or(qPost.content.containsIgnoreCase(keyword));
    }
}