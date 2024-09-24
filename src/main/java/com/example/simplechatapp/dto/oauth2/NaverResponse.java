package com.example.simplechatapp.dto.oauth2;

import java.util.Map;

public class NaverResponse implements OAuth2Response {

    private final Map<String, Object> attribute;

    public NaverResponse(Map<String, Object> attribute) {
        this.attribute = (Map<String, Object>) attribute.get("response");
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getProviderId() {
        return attribute.get("id").toString();
    }

    @Override
    public String getEmail() {
        return attribute.get("email").toString()+"_naver";
    }

    @Override
    public String getName() {
        return attribute.get("name").toString();
    }

    @Override
    public String getNickname() {
        return attribute.get("nickname").toString();
    }

    @Override
    public String getGender() {
        return  attribute.get("gender").toString();
    }

    @Override
    public String getAge() {
        return attribute.get("age").toString();
    }

    @Override
    public String getMobile() {
        return attribute.get("mobile").toString();
    }

    @Override
    public String getImg() {
        return attribute.get("profile_image").toString();
    }
}

