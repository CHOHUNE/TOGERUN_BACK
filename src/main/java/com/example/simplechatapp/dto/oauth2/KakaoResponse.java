package com.example.simplechatapp.dto.oauth2;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class KakaoResponse implements OAuth2Response {

    private final Map<String, Object> attribute;

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return attribute.get("id").toString();
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attribute.get("kakao_account");
        return (String) kakaoAccount.get("email")+"_kakao";
    }

    @Override
    public String getName() {
        return getNickname(); // Kakao doesn't provide a separate "name" field
    }

    @Override
    public String getNickname() {
        Map<String, Object> properties = (Map<String, Object>) attribute.get("properties");
        return (String) properties.get("nickname");
    }

    @Override
    public String getGender() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attribute.get("kakao_account");
        return (String) kakaoAccount.get("gender");
    }

    @Override
    public String getAge() {
        // Kakao doesn't provide exact age, but we can return birthday if needed
        Map<String, Object> kakaoAccount = (Map<String, Object>) attribute.get("kakao_account");
        return (String) kakaoAccount.get("birthday");
    }

    @Override
    public String getMobile() {
        // Kakao doesn't provide mobile number in this response
        return "";
    }

    @Override
    public String getImg() {
        Map<String, Object> properties = (Map<String, Object>) attribute.get("properties");
        return (String) properties.get("profile_image");
    }
}