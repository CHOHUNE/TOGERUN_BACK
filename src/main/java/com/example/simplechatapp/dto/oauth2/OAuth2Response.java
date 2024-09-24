package com.example.simplechatapp.dto.oauth2;

public interface OAuth2Response {

    String getProvider(); // google, naver 인지
    String getProviderId();
    String getEmail();
    String getName();

    String getNickname();
    String getGender();
    String getAge();
    String getMobile();
    String getImg();




}
