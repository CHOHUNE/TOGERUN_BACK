package com.example.simplechatapp;

import com.example.simplechatapp.entity.UserRole;
import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Log4j2
public class UserRepositoryTest {


      @Autowired
     UserRepository userRepository;
    @Autowired
     PasswordEncoder passwordEncoder;

    @DisplayName("회원가입테스트")
    @Transactional
    @Rollback(value = false)
    @Test
    public void 회원가입테스트() {

        for (int i = 0; i <= 10; i++) {

        User user = User.builder()
                .email("user" + i)
                .password(passwordEncoder.encode("1111"))
                .nickname("사용자" + i)
                .build();


        if(i>7) user.addRole(UserRole.ADMIN);
        if(i>0) user.addRole(UserRole.USER);

        userRepository.save(user);



        }
    }

    @Test
    @DisplayName("멤버 읽기")
    public void 멤버읽기() {

        String email = "user1";


        User user = userRepository.getWithRole(email);

        log.info(user);
        log.info(user.getUserRoleList());

        //User(email=user1, nickname=사용자1, social=false, memberRoleList=[USER]
        // EntityGraph 로 한방 쿼리로  N+1 없이 memberRoleList 까지 가져옴


    }
}
