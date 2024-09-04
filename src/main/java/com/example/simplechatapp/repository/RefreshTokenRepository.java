package com.example.simplechatapp.repository;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {


    private final StringRedisTemplate stringRedisTemplate;

    public void saveRefreshToken(String email, String refreshToken, long expirationTime) {
        stringRedisTemplate.opsForValue().set(
                "refresh_token:"+email,
                refreshToken,
                expirationTime,
                TimeUnit.MILLISECONDS
        );
    }


    public String getRefreshToken(String email) {
        return stringRedisTemplate.opsForValue().get("refresh_token:" + email);
    }

    public void deleteRefreshToken(String email) {
        stringRedisTemplate.delete("refresh_token:" + email);
    }

}
