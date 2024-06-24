package com.example.simplechatapp.security;

import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@Log4j2
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    // 일반적으로 UseDetail 은  DB에서 꺼내온 User 의 정보를 담는다
    // Authentication 객체와 차이점이 뭐냐면 Authentication 은 principal, credential, authorities 을 담는 객체이며
    // principal : 인증된 사용자의 정보를 담는 객체 ,
    // credential : 인증된 사용자의 비밀번호를 담는 객체,
    // authorities : 인증된 사용자의 권한을 담는 객체

    // Authentication 객체의 principal 이 UserNameDetailService 리턴값 UserDetail 이 담긴다.


    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.info("--------------------loadByUserName--------------------");

        User user = userRepository.getWithRole(username);

        if(user==null) throw new UsernameNotFoundException("NOT FOUND");

        UserDTO userDTO = new UserDTO(
                user.getEmail(),
                user.getPassword(),
                user.getNickname(),
                user.isSocial(),
                user.getUserRoleList().stream().map(Enum::name).toList());

               // user.getMemberRoleList().stream().map(memberRole -> memberRole.name()).collect(Collectors.toUnmodifiableList()));
                //Enum::name 은 열거형 타입을 name에 할당 해준다. java 16 부터는 toList() 가 .collect(Collectors.toUnmodifiableList()) 과 동일하게 불변 리스트로 만들어 준다.

        return userDTO;
    }
}
