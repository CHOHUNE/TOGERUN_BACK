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
@ToString(exclude = {"chatRoomAsUser1", "chatRoomAsUser2", "userRoleList"})
public class User {

    @Id
    private String email;

    private String nickname;
    private String password;
    private boolean social;

    @ElementCollection(fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserRole> userRoleList = new ArrayList<>();

    @OneToMany(mappedBy = "writer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatRoom> chatRoomAsCreator = new ArrayList<>();

    @OneToMany(mappedBy = "subscriber", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatRoom> chatRoomAsParticipant = new ArrayList<>();

    @OneToMany(mappedBy = "organizer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ExerciseEvent> organizedEvents = new ArrayList<>();

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

}
