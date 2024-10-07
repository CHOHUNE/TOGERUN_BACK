package com.example.simplechatapp.repository;

import com.example.simplechatapp.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface FavoriteRepository extends JpaRepository<Favorite, Long> {


    @Query("SELECT f FROm Favorite  f JOIN FETCH  f.post WHERE f.user.email = :email AND f.isActive =true ")
    List<Favorite> findAllByEmail(String email);

    Optional<Favorite> findByUserIdAndPostId(Long userId, Long postId);
}
