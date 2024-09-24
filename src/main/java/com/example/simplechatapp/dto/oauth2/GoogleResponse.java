package com.example.simplechatapp.dto.oauth2;


import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class GoogleResponse implements OAuth2Response {

    private final Map<String,Object > attribute;

    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public String getProviderId() {
        return attribute.get("sub").toString();
    }

    @Override
    public String getEmail() {
        return attribute.get("email").toString()+"_google";
    }

    @Override
    public String getName() {
        return attribute.get("name").toString();
    }

    @Override
    public String getNickname() {
        return "";
    }

    @Override
    public String getGender() {
        return "";
    }

    @Override
    public String getAge() {
        return null;
    }

    @Override
    public String getMobile() {
        return "";
    }

    @Override
    public String getImg() {
        return attribute.get("picture").toString();
    }


}

