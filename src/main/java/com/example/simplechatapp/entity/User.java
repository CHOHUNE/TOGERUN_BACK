package com.example.simplechatapp.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @Enumerated(EnumType.STRING) // 번호로 사용시 추후 오류 발생을 미연에 방지
    @Builder.Default
    private List<UserRole> userRoleList = new ArrayList<>();

//    @OneToMany(mappedBy = "organizer", cascade = CascadeType.ALL, orphanRemoval = true)
//    @Builder.Default
//    private List<ExerciseEvent> organizedEvents = new ArrayList<>();

    @ManyToMany(mappedBy = "participants")
    @Builder.Default
    @JsonBackReference
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


// 편의관계 메서드는 ChatRoom 쪽에만 쓸 예정 -> 굳이 살려둔다면 아래 방식으로 중간 테이블만 변경해서 관리 해준다.

    public void joinChatRoom(ChatRoom chatRoom) {
        if (!this.joinedChatRoom.contains(chatRoom)) {
//            this.joinedChatRoom.add(chatRoom);
            chatRoom.getParticipants().add(this);
        }
    }

    public void leaveChatRoom(ChatRoom chatRoom) {
            chatRoom.getParticipants().remove(this);
    }

}
