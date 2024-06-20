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
@ToString(exclude = "password")
public class User {

    @Id
    private String email;

    private String nickname;
    private String password;
    private boolean social;

    @ElementCollection(fetch = FetchType.LAZY)
    @Builder.Default
    private List<MemberRole> memberRoleList = new ArrayList<>();

    @OneToMany(mappedBy = "user1", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoom> chatRoomAsUser1 = new ArrayList<>();

    @OneToMany(mappedBy = "user1", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoom> chatRoomAsUser2 = new ArrayList<>();


    public void addRole(MemberRole memberRole) {
        memberRoleList.add(memberRole);
    }

    public void clearRole() {
        memberRoleList.clear();
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
