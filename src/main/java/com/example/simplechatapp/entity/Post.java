package com.example.simplechatapp.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
//@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="post")
@ToString(exclude = {"imageList", "user", "chatRoom", "comments", "favorites", "likes"})
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

    private LocalDate localDate; //게시글 작성 시간


    @JoinColumn(name="meeting_time")
    private LocalDateTime meetingTime; //집결 시간

    private boolean delFlag;

    private Double latitude;
    private Double longitude;
    private String placeName;
    private String roadName;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType activityType;

    @Column(nullable = false)
    private Integer capacity;

    @OneToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name="chat_room_id")
    @JsonManagedReference
    private ChatRoom chatRoom;


    @ElementCollection // 생명주기 관리 자체가 게시물 = 게시물 이미지 이므로 ManyToOne 과 Cascade 를 안쓴다.
    @CollectionTable(name="post_images", joinColumns = @JoinColumn(name="post_id"))
    @OrderColumn(name="ord")
    @Column(name="file_name")
    @Builder.Default
    private List<PostImage> imageList = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Favorite> favorites = new ArrayList<>(); // Favorite 가 주인

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Like> likes = new ArrayList<>(); //LIKE 가 주인

    // mappedBy 가 있는 쪽은 읽기 전용이다. 즉, 데이터를 수정할 수 없다.
    // 즉 관계의 주인이 아니란 것 -> 데이터베이스 외래 키를 가지고 있지 않음
    // mappedBy 가 없는 쪽이 관계의 주인이다. -> 데이터베이스 외래 키를 가지고 있음

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long viewCount;
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean participateFlag;



    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Notify> notifications = new ArrayList<>();

    public void addNotification(Notify notify) {

        this.notifications.add(notify);
        notify.setPost(this);
    }



    public void changeUser(User user) {
        this.user = user;
    }

    public void changeTitle(String title) {
        this.title = title;
    }

    public void changeContent(String content) {
        this.content = content;
    }

    public void changeDelFlag(boolean delFlag) {
        this.delFlag = delFlag;
    }

    public void changePlaceName(String placeName) {
        this.placeName = placeName;
    }

    public void changeLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void changeLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void changeMeetingTime(LocalDateTime meetingTime) {
        this.localDate = meetingTime.toLocalDate();
    }

    public void addImage(PostImage image) {

        imageList.add(image);

    }

    public void changeActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }

    public void changeCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public void changeViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public void changeParticipateFlag(Boolean participateFlag) {
        this.participateFlag = participateFlag;
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


    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", user=" + user +
                ", localDate=" + localDate +
                ", meetingTime=" + meetingTime +
                ", delFlag=" + delFlag +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", placeName='" + placeName + '\'' +
                ", chatRoom=" + chatRoom +
                ", imageList=" + imageList +
                ", comments=" + comments +
                ", favorites=" + favorites +
                ", likes=" + likes +
                ", viewCount=" + viewCount +
                ", participateFlag=" + participateFlag +
                '}';
    }

    public void clearList() {
        this.imageList.clear();
    }

}
