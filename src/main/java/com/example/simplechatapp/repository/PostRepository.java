package com.example.simplechatapp.repository;

import com.example.simplechatapp.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface PostRepository extends JpaRepository<Post,Long>,PostSearch {

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + :viewCount WHERE p.id = :postId")
    void incrementViewCount(Long postId,Long viewCount);


    @Modifying
    @Query("""
        UPDATE Post p 
        SET p.participateFlag = false 
        WHERE p.meetingTime < :now 
        AND p.participateFlag = true
        """)
    int bulkUpdateParticipateFlag(LocalDateTime now);

//    @Query("select p from Post p where p.meetingTime < :now")
//    List<Post> findByMeetingTimeBefore(LocalDateTime now);

}
