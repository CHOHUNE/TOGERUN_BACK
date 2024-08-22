package com.example.simplechatapp.repository;

import com.example.simplechatapp.dto.PageRequestDTO;
import com.example.simplechatapp.dto.PostDTO;
import com.example.simplechatapp.entity.*;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;
import java.util.Optional;

public class PostSearchImpl extends QuerydslRepositorySupport implements PostSearch {

    public PostSearchImpl() {
        super(Post.class);
    }

    @Override
    public Page<Post> search1(PageRequestDTO pageRequestDTO) {
        QPost qPost = QPost.post;
        QUser qUser = QUser.user;

        JPQLQuery<Post> query = from(qPost)
                .leftJoin(qPost.user, qUser).fetchJoin()
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

    public Optional<PostDTO> findPostWithLikeAndFavorite(Long postId, Long userId) {
        QPost qPost = QPost.post;
        QUser qUser = QUser.user;
        QLike qLike = QLike.like;
        QFavorite qFavorite = QFavorite.favorite;

        PostDTO postDTO = from(qPost)
                .leftJoin(qPost.user, qUser)
                .leftJoin(qLike).on(qLike.post.eq(qPost).and(qLike.user.id.eq(userId)))
                .leftJoin(qFavorite).on(qFavorite.post.eq(qPost).and(qFavorite.user.id.eq(userId)))
                .where(qPost.id.eq(postId))
                .select(Projections.constructor(PostDTO.class,
                        qPost.id,
                        qPost.title,
                        qPost.content,
                        qUser.id,
                        qUser.nickname,
                        qPost.localDate,
                        qPost.delFlag,
                        qFavorite.isActive.coalesce(false),
                        JPAExpressions.select(qLike.count())
                                .from(qLike)
                                .where(qLike.post.eq(qPost).and(qLike.isActive.isTrue())),
                        qLike.isActive.coalesce(false),
                        qPost.placeName,
                        qPost.latitude,
                        qPost.longitude,
                        qPost.meetingTime
                ))
                .fetchOne();

        return Optional.ofNullable(postDTO);
    }


    private BooleanExpression containsKeyword(QPost qPost, String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return null;
        }
        return qPost.title.containsIgnoreCase(keyword)
                .or(qPost.content.containsIgnoreCase(keyword));
    }
}