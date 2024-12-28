package com.example.simplechatapp.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisRateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;

    public boolean tryAcquire(String key, int maxRequests, Duration duration) {

        String rateLimitKey = "rate_limit:" + key;

        try {

            return Boolean.TRUE.equals(redisTemplate.execute(new SessionCallback<Boolean>() {
                @Override
                public Boolean execute(RedisOperations operations) throws DataAccessException {
                    operations.multi();

                    operations.opsForValue().increment(rateLimitKey, 1);
                    operations.expire(rateLimitKey, duration);

                    List<Object> results = operations.exec();
                    if (results == null) {
                        return false;
                    }

                    Long currentCount = (Long) results.get(0);
                    return currentCount <= maxRequests;

                }
            }));
        } catch (Exception e) {
            log.error("Rate limit check failed for key: {}", key, e);
            return true;
        }
    }
}

