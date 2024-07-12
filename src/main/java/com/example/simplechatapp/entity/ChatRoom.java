package com.example.simplechatapp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Table(name = "chat_rooms")
@ToString(exclude = {"participants","chatMessageList"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY,mappedBy = "chatRoom")
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

    @Builder.Default
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ChatMessage> chatMessageList = new ArrayList<>();


    // 편의 메서드 추가

    public void addParticipant(User user) {
        if (!hasParticipant(user)) {
            this.participants.add(user);
            user.getJoinedChatRoom().add(this);
        }
    } //  hasParticipant 로 검증 과정 추가

    public void removeParticipant(User user) {
        if (hasParticipant(user)) {
            this.participants.remove(user);
            user.getJoinedChatRoom().remove(this);
        }
    }

    public void addChatMessage(ChatMessage chatMessage) {
        this.chatMessageList.add(chatMessage);
        chatMessage.setChatRoom(this);
    }

    public void setPost(Post post) {
        this.post=post;
        if (post != null && post.getChatRoom() != this) {
            post.setChatRoom(this);
        }
    }

    public boolean hasParticipant(User user) {
        return participants.contains(user);

        //있으면 true 없으면 false
    }




//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name="writer_id")
//    private User writer;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name="subscriber_id")
//    private User subscriber;

//    @ManyToOne
//    private ExerciseEvent exerciseEvent;

}


