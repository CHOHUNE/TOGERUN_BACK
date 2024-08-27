package com.example.simplechatapp.repository;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public void saveRefreshToken(String email, String refreshToken,long expirationTime) {
        redisTemplate.opsForValue().set(
                "refresh_token:"+email,
                refreshToken,
                expirationTime,
                TimeUnit.MILLISECONDS
        );
    }


    public String getRefreshToken(String email) {
        return redisTemplate.opsForValue().get("refresh_token:"+email);
    }

    public void deleteRefreshToken(String email) {
        redisTemplate.delete("refresh_token" + email);
    }

}
