package com.example.simplechatapp.dto.oauth2;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenInfo {

    private String accessToken;
    private String refreshToken;

}
