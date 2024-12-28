package com.example.simplechatapp.aop;


import com.example.simplechatapp.annotation.RateLimit;
import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.service.RedisRateLimitService;
import com.example.simplechatapp.util.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedisRateLimitService redisRateLimitService;


    @Around("@annotation(rateLimit)") // 설명: rateLimit 어노테이션이 붙은 메소드에 적용
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {

        String key = generateKey(joinPoint);

        if(!redisRateLimitService.tryAcquire(key, rateLimit.maxRequests(),
                Duration.ofSeconds(rateLimit.duration()))) {

            throw new RateLimitExceededException("요청 제한 초과");
        }

        return joinPoint.proceed();
    }


    private String generateKey(ProceedingJoinPoint joinPoint) {

        String methodName = joinPoint.getSignature().toShortString(); // 메서드 네임

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();

            return String.format("%s:user:%d", methodName, user.getId());
        }
        // 비인증 사용자는 IP 주소로 구분
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String clientIp = "unknown";
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            clientIp = request.getRemoteAddr();
        }

        return String.format("%s:ip:%s", methodName, clientIp);
    }
}


