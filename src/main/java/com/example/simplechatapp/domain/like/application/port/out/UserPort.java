package com.example.simplechatapp.domain.like.application.port.out;

import com.example.simplechatapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/*
* 사용자 정보 조회 포트
* */
@Component
public interface UserPort  {
    Optional<Long> findUserIdByEmail(String email);
}
