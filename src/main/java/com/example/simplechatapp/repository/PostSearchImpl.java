package com.example.simplechatapp.repository;

import com.example.simplechatapp.dto.PageRequestDTO;
import com.example.simplechatapp.dto.PostDTO;
import com.example.simplechatapp.entity.*;
import com.querydsl.core.types.dsl.BooleanExpression;
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

    @Override
    public Optional<PostDTO> findPostWithLikeAndFavorite(Long postId, Long userId) {
        QPost qPost = QPost.post;
        QUser qUser = QUser.user;
        QLike qLike = QLike.like;
        QFavorite qFavorite = QFavorite.favorite;

        Post post = from(qPost)
                .leftJoin(qPost.user, qUser).fetchJoin()
                .leftJoin(qPost.imageList).fetchJoin()  // Fetch join for imageList
                .leftJoin(qLike).on(qLike.post.eq(qPost).and(qLike.user.id.eq(userId)))
                .leftJoin(qFavorite).on(qFavorite.post.eq(qPost).and(qFavorite.user.id.eq(userId)))
                .where(qPost.id.eq(postId))
                .select(qPost)
                .fetchOne();

        if (post != null) {
            Long likeCount = from(qLike)
                    .where(qLike.post.eq(post).and(qLike.isActive.isTrue()))
                    .select(qLike.count())
                    .fetchOne();

            Boolean isFavorite = from(qFavorite)
                    .where(qFavorite.post.eq(post).and(qFavorite.user.id.eq(userId)))
                    .select(qFavorite.isActive)
                    .fetchOne();

            Boolean isLike = from(qLike)
                    .where(qLike.post.eq(post).and(qLike.user.id.eq(userId)))
                    .select(qLike.isActive)
                    .fetchOne();

            return Optional.of(convertToDTO(post, likeCount, isFavorite, isLike));
        }

        return Optional.empty();
    }

    private PostDTO convertToDTO(Post post, Long likeCount, Boolean isFavorite, Boolean isLike) {
        return PostDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .userId(post.getUser().getId())
                .nickname(post.getUser().getNickname())
                .localDate(post.getLocalDate())
                .delFlag(post.isDelFlag())
                .isFavorite(isFavorite != null ? isFavorite : false)
                .likeCount(likeCount != null ? likeCount : 0L)
                .isLike(isLike != null ? isLike : false)
                .placeName(post.getPlaceName())
                .latitude(post.getLatitude())
                .longitude(post.getLongitude())
                .meetingTime(post.getMeetingTime())
                .imageList(post.getImageList().stream().map(PostImage::getFileName).toList())
                .build();
    }


    private BooleanExpression containsKeyword(QPost qPost, String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return null;
        }
        return qPost.title.containsIgnoreCase(keyword)
                .or(qPost.content.containsIgnoreCase(keyword));
    }
}