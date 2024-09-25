package com.example.simplechatapp.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
//@Table(name = "comment") 이름이 같은 경우 생략 가능
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id")
//    private User user;

    private String createdBy; // 이메일을 or 아이디를 넣을지 고민 -> 이메일
    private String name;
    private String img;

    // 소규모 프로젝트, 성능을 중시하는 경우에 단순히 createdBy를 사용해도 무방
    // 대규모 프로젝트, 무결성을 중시하는 겨우에는 User 객체를 사용하는 것이 좋음

    @Column(nullable = false)
    @Lob
    private String content;

    private boolean delFlag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Comment> children = new ArrayList<>();

    private LocalDateTime createdAt;

    @Builder
    public Comment(Post post, String createdBy, String content, Comment parent, String name, String img) {
        this.post = post;
        this.createdBy = createdBy;
        this.content = content;
        this.parent = parent;
        this.createdAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        this.name = name;
        this.img = img;

    }

    public void update(String content) {
        this.content = content;
    }


}