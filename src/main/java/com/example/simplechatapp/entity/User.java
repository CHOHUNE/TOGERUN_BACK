package com.example.simplechatapp.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = { "userRoleList"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String nickname;
    private String password;
    private boolean social;

    @ElementCollection(fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserRole> userRoleList = new ArrayList<>();

//    @OneToMany(mappedBy = "organizer", cascade = CascadeType.ALL, orphanRemoval = true)
//    @Builder.Default
//    private List<ExerciseEvent> organizedEvents = new ArrayList<>();

    @ManyToMany(mappedBy = "participants")
    @Builder.Default
    private List<ChatRoom> joinedChatRoom = new ArrayList<>();

    public void addRole(UserRole userRole) {
        userRoleList.add(userRole);
    }

    public void clearRole() {
        userRoleList.clear();
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changePw(String pw) {
        this.password = pw;
    }

    public void changeSocial(boolean social) {
        this.social = social;
    }

    // -- 양방향 관계 편의 메서드 --

    public void joinChatRoom(ChatRoom chatRoom) {
        if (!this.joinedChatRoom.contains(chatRoom)) {
            this.joinedChatRoom.add(chatRoom);
            chatRoom.getParticipants().add(this);
        }
    }

    public void leaveChatRoom(ChatRoom chatRoom) {

        if (this.joinedChatRoom.remove(chatRoom)) {
            chatRoom.getParticipants().remove(this);
        }
    }

}
