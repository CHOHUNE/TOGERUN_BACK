package com.example.simplechatapp.service;

import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.dto.UserModifyDTO;
import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.entity.UserRole;
import com.example.simplechatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.LinkedHashMap;
import java.util.Optional;


@Service
@Log4j2
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;



    @Override
    public UserDTO getKakaoMember(String accessToken) {

        String nickname = getEmailFromKakaoAccessToken(accessToken);

        Optional<User> result = userRepository.findById(nickname);

        if (result.isPresent()) {

            UserDTO userDTO =  entityToDTO(result.get());

            log.info("existedMember{}", userDTO);

            return userDTO;
        }

        User socialUser = makeSocialUser(nickname);

        userRepository.save(socialUser);

        UserDTO userDTO = entityToDTO(socialUser);



        return userDTO;
    }

    @Override
    public void modifyMember(UserModifyDTO userModifyDTO) {

        Optional<User> result = userRepository.findById(userModifyDTO.getEmail());

        User user = result.orElseThrow();

        user.changeNickname(userModifyDTO.getNickname());
        user.changeSocial(false);
        user.changePw(passwordEncoder.encode(userModifyDTO.getPw()));

         userRepository.save(user);

    }

    private User makeSocialUser(String email) {

        String tempPassword = makeTempPassword();
        log.info("tempPassword{}", tempPassword);

        User user = User.builder().email(email)
                .password(passwordEncoder.encode(tempPassword))
                .nickname("kakao"+"_"+email)
                .social(true)
                .build();

        user.addRole(UserRole.USER);

        return user;
    }

    private String getEmailFromKakaoAccessToken(String accessToken) {

        String kakaoGetUserURL = "https://kapi.kakao.com/v2/user/me";

        if (accessToken == null) {

            throw new RuntimeException("Access Token is NULL");

        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<Object> entity = new HttpEntity<>(headers);

        UriComponents uriBuilder = UriComponentsBuilder.fromHttpUrl(kakaoGetUserURL).build();
        //URI 빌더를 이용해 endPoint 를 URI 객체로 변환한다
        // String -> HttpURI

        //GET 메서드로 API 호출 : restTemplate exchange 메서드를 사용하여 GET 요청을 보내고 그 결과를
        // ResponseEntity 타입으로 받고 있다. toURI 는 실제 호출할 몌ㅑ endPoint, GET 요청, entity는 필요한 헤더와 본문

        ResponseEntity<LinkedHashMap> response =
                restTemplate.exchange(uriBuilder.toUri(), HttpMethod.GET, entity, LinkedHashMap.class);

        log.info(response);

        LinkedHashMap<String, LinkedHashMap> bodyMap = response.getBody();

        log.info("------------------------------------");
        log.info("bodyMap{}",bodyMap);

        LinkedHashMap<String, String> kakaoAccount = bodyMap.get("properties");

        log.info("kakaoAccount: {}", kakaoAccount);

        String nickname = kakaoAccount.get("nickname");

        log.info("nickname{}", nickname);

        return nickname;

    }

    private String makeTempPassword() {

        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < 10; i++) {

            buffer.append((char) ((Math.random() * 26) + 97));

        }

        return buffer.toString();
    }

}
