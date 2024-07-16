package com.example.simplechatapp.repository;

import com.example.simplechatapp.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;

public
interface UserRepository extends JpaRepository<User,String> {

    @EntityGraph(attributePaths = "userRoleList")
    @Query("SELECT u FROM User u WHERE u.email = :email")
    User getWithRole(@Param("email")String email);

    boolean existsByNickname(String nickname);

    @Query("SELECT u FROM User u WHERE u.email = :email")
    User findByEmail(String email);



}
