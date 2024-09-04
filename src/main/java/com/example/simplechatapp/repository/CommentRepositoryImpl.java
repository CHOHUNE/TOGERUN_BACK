package com.example.simplechatapp.repository;

import com.example.simplechatapp.entity.Comment;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.simplechatapp.entity.QComment.comment;


@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CustomCommentRepository {



    private final JPAQueryFactory jpaQueryFactory;
    // JpaQueryFactory : Querydsl을 사용하기 위한 클래스
    //JPQL을 사용하는 것과 비슷하게 생각하면 된다.
    // Querydsl Config 에서 주입받음


    //댓글 및 대댓글 조회 , n+1 문제 방지, 부모 댓글의 ID(parent.id) 기준으로 오름차순으로 정렬하며, 부모 댓글이 없는 경우(nulls)는 리스트의 가장 앞에 위치
    @Override

    public List<Comment> findCommentByPostId(Long postId) {

        return jpaQueryFactory.selectFrom(comment)
                .leftJoin(comment.post).fetchJoin()
                .where(comment.post.id.eq(postId))
                .orderBy(comment.parent.id.asc().nullsFirst())
                .fetch();

    }

}
