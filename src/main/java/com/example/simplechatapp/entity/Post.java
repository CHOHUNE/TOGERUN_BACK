package com.example.simplechatapp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="post")
@ToString(exclude = {"imageList","user"})
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length = 100)
    private String title;
    private String content;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    private LocalDate localDate;

    private boolean delFlag;

    @OneToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name="chat_room_id")
    private ChatRoom chatRoom;


    @ElementCollection // 생명주기 관리 자체가 게시물 = 게시물 이미지 이므로 ManyToOne 과 Cascade 를 안쓴다.
    @Builder.Default
    private List<PostImage> imageList = new ArrayList<>();


    public void changeTitle(String title) {
        this.title = title;
    }

    public void changeContent(String content) {
        this.content = content;
    }

    public void changeDelFlag(boolean delFlag) {
        this.delFlag = delFlag;
    }

    public void addImage(PostImage image) {

        image.setOrd(imageList.size());
        imageList.add(image);

    }

    public void addImageString(String fileName) {
        PostImage postImage = PostImage.builder().fileName(fileName).build();

        addImage(postImage);
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
        if (chatRoom != null && chatRoom.getPost() != this) {
            chatRoom.setPost(this);
        }
    }

    public void clearList() {
        this.imageList.clear();
    }

}
