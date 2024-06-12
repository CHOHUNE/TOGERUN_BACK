package com.example.simplechatapp.repository;

import com.example.simplechatapp.dto.PageRequestDTO;
import com.example.simplechatapp.dto.PageResponseDTO;
import com.example.simplechatapp.entity.Post;
import com.example.simplechatapp.entity.QPost;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class PostSearchImpl extends QuerydslRepositorySupport implements PostSearch{

    public PostSearchImpl() {
        super(Post.class);
    }


    @Override
    public Page<Post> search1(PageRequestDTO pageRequestDTO) {

        QPost qPost = QPost.post;

        JPQLQuery<Post> query = from(qPost);

        query.where(qPost.title.contains("1"));

        Pageable pageable = PageRequest.of(pageRequestDTO.getPage() - 1, pageRequestDTO.getSize(), Sort.by("id").descending());

        this.getQuerydsl().applyPagination(pageable, query);

        List<Post> list = query.fetch();

        long total = query.fetchCount();

        return new PageImpl<>(list, pageable, total);

    }
}
