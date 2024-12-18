package com.example.simplechatapp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Table(name = "chat_rooms")
@ToString(exclude = {"participants", "chatMessageList"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "chatRoom")
    @JsonBackReference
    private Post post;

    @ManyToMany(fetch = FetchType.LAZY)
//    @JoinColumn(name="user_id")
    @JoinTable(
            name = "chat_room_participants",
            joinColumns = @JoinColumn(name = "chat_room_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> participants = new HashSet<>();
    //SET 으로 한 이유 : 중복을 허용하지 않기 위함
    // 기획 의도를 게시글 - > 1 : 1 채팅방에서 러닝 단체 채팅방으로 기획의도를 변경해서
    // ManyToMany 로 엔티티 변경 -> OneToMany 로 변경

    private int participantCount;
    private boolean isParticipant;
    private boolean canJoin;
    private ActivityType activityType;



    @Builder.Default
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ChatMessage> chatMessageList = new ArrayList<>();


    // 편의 메서드 추가

    public void addParticipant(User user) {
        if (!hasParticipant(user)) {
            this.participants.add(user);
//            user.getJoinedChatRoom().add(this); -> 중간 테이블인 participants 만 관리하면 자동으로 동기화 된다. -> 불필요한 코드

        }
    } //  hasParticipant 로 검증 과정 추가

    public void removeParticipant(User user) {
        this.participants.remove(user);
//            user.leaveChatRoom(this); // 마찬가지로 중간 테이블만 관리

    }

    public void addChatMessage(ChatMessage chatMessage) {
        this.chatMessageList.add(chatMessage);
        chatMessage.setChatRoom(this);
    }

    public void setPost(Post post) {
        this.post = post;
        if (post != null && post.getChatRoom() != this) {
            post.setChatRoom(this);
        }
    }

    public boolean hasParticipant(User user) {
        return participants.contains(user);

        //있으면 true 없으면 false
    }
}


