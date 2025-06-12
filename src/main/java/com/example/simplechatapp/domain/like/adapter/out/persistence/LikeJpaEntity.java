package com.example.simplechatapp.domain.like.adapter.out.persistence;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_like")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LikeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private boolean isActive;
}
