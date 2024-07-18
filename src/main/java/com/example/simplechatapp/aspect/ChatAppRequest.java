package com.example.simplechatapp.aspect;

import com.example.simplechatapp.entity.NotificationType;
import com.example.simplechatapp.entity.NotifyMessage;
import com.example.simplechatapp.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.springframework.scheduling.annotation.Async;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@ToString(exclude = "chatApp")
public class ChatAppRequest implements NotifyInfo{



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ChatAppRequestId;

    @Column(length = 100, nullable = false)
    private String title;

    @Override
    public User getReceiver() {

        return chat.getUser();
    }

    @Override
    public Long getGoUrlId() {
        return chat.getChatId;
    }

    @Override
    public NotificationType getNotificationType() {
        return NotificationType.CHAT;
    }

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
