package com.example.simplechatapp.repository;

import com.example.simplechatapp.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public
interface UserRepository extends JpaRepository<User,Long> {

    @EntityGraph(attributePaths = "userRoleList")
    @Query("SELECT u FROM User u WHERE u.email = :email")
    User getWithRole(@Param("email")String email);

    boolean existsByNickname(String nickname);

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(String email);


    @Query("SELECT u FROM User u " +
           "LEFT JOIN FETCH u.joinedChatRoom cr " +
           "LEFT JOIN FETCH cr.post " +
           "LEFT JOIN FETCH cr.chatMessageList " +
           "WHERE u.email = :email")
    Optional<User> findByEmailWithChatRooms(@Param("email") String email);

}
