package com.example.simplechatapp.repository;

import com.example.simplechatapp.entity.Post;
import com.example.simplechatapp.entity.QPost;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

public class PostSearchImpl extends QuerydslRepositorySupport implements PostSearch{

    public PostSearchImpl() {
        super(Post.class);
    }

    @Override
    public Page<Post> search1() {

        QPost qpost = QPost.post;

        JPQLQuery<Post> query = from(qpost);
        // select * from post

        query.where(qpost.title.contains("1"));

        Pageable pageable = PageRequest.of(1, 10, Sort.by("id").descending());

        this.getQuerydsl().applyPagination(pageable, query);

        query.fetch();
        query.fetchCount();


        return null;
    }
}
