package com.example.simplechatapp.service;


import com.example.simplechatapp.dto.oauth2.OAuth2Response;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        //super 키워드는 자식 클래스에서 부모 클래스의 메서드나 생성자를 호출할 때 사용 된다.
        // DefaultOAuth2UserService 의 loadUser 를 사용한다고 보면 된다.

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // registrationId 란?
        OAuth2Response oAuth2Response = null;

        if (registrationId.equals("naver")) {



        }

    }


}
