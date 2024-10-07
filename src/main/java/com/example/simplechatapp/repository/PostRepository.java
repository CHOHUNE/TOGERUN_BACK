package com.example.simplechatapp.repository;

import com.example.simplechatapp.entity.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post,Long>,PostSearch {

    @EntityGraph(attributePaths = "imageList")
    @Query("select p from Post p where p.id = :id")
    Optional<Post> selectOne(@Param("id") Long id);

    // 엔티티 그래프는 fetchJoin 과 함께 N+1 문제를 해결할 때 쓰는 어노테이션이다.
    // N+1 문제는 한개의 쿼리를 데이터로 가져온 후 가져온 데이터의 개수만큼 추가 쿼리가 실행되는 문제다.
    //EntityGraph 는  Post 관련 엔티티를 조회할 때 imageList 엔티티도 함께 조회하도록 실행한다

    //fetchJoin 과 비교 했을 때 EntityGraph 는 JPQL 에서 사용할 수 있고 fetchJoin 은 쿼리 메소드에서 사용할 수 있다.
    // 간단한 쿼리에서는 fetchJoin 복잡한 쿼리에서는 EntityGraph 를 좀 더 유연하게 쓸 수 있다.

    @Modifying
    @Query(" update Post p set p.delFlag= :delFlag where p.id = :id")
    void updateToDelete(@Param("id") Long id, @Param("delFlag") boolean delFlag);


    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount +1 WHERE p.id = :postId")
    void incrementViewCount(Long postId);



    List<Post> findByMeetingTimeBefore(LocalDateTime now);

}
