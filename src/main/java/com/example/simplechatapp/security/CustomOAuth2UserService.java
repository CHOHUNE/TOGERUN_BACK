package com.example.simplechatapp.security;


import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.dto.oauth2.*;
import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.entity.UserRole;
import com.example.simplechatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        //super 키워드는 자식 클래스에서 부모 클래스의 메서드나 생성자를 호출할 때 사용 된다.
        // DefaultOAuth2UserService 의 loadUser 를 사용한다고 보면 된다.

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // registrationId 는 OAuth2 공급자의 아이디
        OAuth2Response oAuth2Response = null;

        if (registrationId.equals("naver")) {

            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());

        } else if (registrationId.equals("google")) {

            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());

        } else if(registrationId.equals("kakao")){
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());

        }else{
            return null;
        }


        String username = oAuth2Response.getEmail();
        User existData = userRepository.getWithRole(username);

        if (existData == null) {

            existData = new User();
            existData.setNickname(oAuth2Response.getNickname());
            existData.setName(oAuth2Response.getName());
            existData.setEmail(oAuth2Response.getEmail());
            existData.setImg(oAuth2Response.getImg());
            existData.setMobile(oAuth2Response.getMobile());
            existData.setAge(oAuth2Response.getAge());
            existData.setGender(oAuth2Response.getGender());

            existData.setSocial(true);
            existData.setUserRoleList(List.of(UserRole.USER));

            userRepository.save(existData);

        }else {

            existData.setNickname(oAuth2Response.getNickname());
            existData.setEmail(oAuth2Response.getEmail());

            userRepository.save(existData);

        }

        UserDTO userDTO = new UserDTO(
                existData.getId(),
                existData.getEmail(),
                "Temp",
                existData.getName(),
                existData.getNickname(),
                true,
                existData.getGender(),
                existData.getAge(),
                existData.getMobile(),
                existData.getImg(),
                List.of("ROLE_USER"));

        return new CustomOAuth2User(userDTO);

        }

}
