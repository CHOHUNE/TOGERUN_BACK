package com.example.simplechatapp.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter

public class Notify {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    private String content;

    private String url;

    @Column(nullable = false)
    private Boolean isRead;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE) // User 가 삭제되면 같이 삭제
    private User receiver;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @Builder
    public Notify (User receiver, NotificationType notificationType, String content, String url, Boolean isRead, LocalDateTime createdAt,Post post) {
        this.receiver = receiver;
        this.notificationType = notificationType;
        this.content = content;
        this.createdAt = createdAt;
        this.url = url;
        this.isRead = isRead;
        this.post = post;
    }

    public Notify() {

    }
}
