package com.example.simplechatapp.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name = "chat_rooms")
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="post_id")
    private Post post;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "chat_room_participants",
            joinColumns = @JoinColumn(name = "chat_room_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();
    //SET 으로 한 이유 : 중복을 허용하지 않기 위함
    // 기획 의도를 게시글 - > 1 : 1 채팅방에서 러닝 단체 채팅방으로 기획의도를 변경해서
    // ManyToMany 로 엔티티 변경

    public void addParticipant(User user) {
        participants.add(user);

    }

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name="writer_id")
//    private User writer;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name="subscriber_id")
//    private User subscriber;

    @ManyToOne
    private ExerciseEvent exerciseEvent;

}



