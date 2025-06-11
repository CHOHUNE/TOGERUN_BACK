package com.example.simplechatapp.domain.like.application.port.out;

import java.util.Optional;

/*
* 사용자 정보 조회 포트
* */
public interface UserPort {
    Optional<Long> findUserIdByEmail(String email);
}
