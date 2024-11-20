package com.example.simplechatapp.repository;

import com.example.simplechatapp.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post,Long>,PostSearch {

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount +1 WHERE p.id = :postId")
    void incrementViewCount(Long postId);

    @Query("select p from Post p where p.meetingTime < :now")
    List<Post> findByMeetingTimeBefore(LocalDateTime now);

}
