package com.example.simplechatapp.aop;

import com.example.simplechatapp.aop.proxy.NotifyInfo;
import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.entity.NotifyMessage;
import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.service.NotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.util.Set;

@Aspect
@Slf4j
@Component
@EnableAsync // 애너테이션을 비동기적으로 처리하겟다는 뜻 - 클래스 단위에 붙인다.
@RequiredArgsConstructor
public class NotifyAspect {


    private final NotifyService notifyService;

    @Pointcut("@annotation(com.example.simplechatapp.annotation.NeedNotify)")
    // 포인트 컷을 해당 어노테이션이 붙은 메소드로 지정하겠다는 설정
    public void annotationPointcut() {

    }

    // annotationPointCut() 메서드를 통해 위 어노테이션에 정의한 메소드들을 대상으로 AOP 적용이 가능해졌다.

    @Async
    @AfterReturning(pointcut = "annotationPointcut()", returning = "result")
    public void checkValue(JoinPoint joinPoint, Object result) throws Throwable {
        NotifyInfo notifyInfo = null;

        if (result instanceof ResponseEntity) {
            ResponseEntity<?> responseEntity = (ResponseEntity<?>) result;
            Object body = responseEntity.getBody();
            if (body instanceof NotifyInfo) {
                notifyInfo = (NotifyInfo) body;
            }
        } else if (result instanceof NotifyInfo) {
            notifyInfo = (NotifyInfo) result;
        }

        if (notifyInfo != null) {
            Set<String> receivers = notifyInfo.getReceiver();

            for (String receiver : receivers) {
                notifyService.send(
                        receiver,
                        notifyInfo.getNotificationType(),
                        notifyInfo.getNotifyMessage().getMessage(),
                        notifyInfo.getGoUrlId()
                );
            }

            log.info("Notification sent for result: {}", notifyInfo);
        } else {
            log.error("Method did not return a NotifyInfo instance or ResponseEntity with NotifyInfo body");
        }
    }}
