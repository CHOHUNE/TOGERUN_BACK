package com.example.simplechatapp.aspect;

import com.example.simplechatapp.entity.NotifyMessage;
import com.example.simplechatapp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
@EnableAsync
@RequiredArgsConstructor
public class NotifyAspect {


    private final NotificationService notificationService;

    @Pointcut("@annotation(com.example.simplechatapp.aspect.NeedNotify)")
    public void annotationPointcut() {

    }
    // annotationPointCut() 메서드를 통해 위 어노테이션에 정의한 메소드들을 대상으로 AOP 적용이 가능해졌다.

    @Async
    @AfterReturning(pointcut = "annotationPointcut()", returning = "result")
    public void checkValue(JoinPoint joinPoint, Object result) throws Throwable {

        NotifyInfo notifyProxy = (NotifyInfo) result;

        notificationService.send(
                notifyProxy.getReceiver(),
                notifyProxy.getNotificationType(),
                NotifyMessage.CHAT_APP_ALERT.getMessage(),
                "/api/notify" + (notifyProxy.getGoUrlId())
        );

        log.info("result ={} ", result);
    }


}
