package com.example.simplechatapp.service;

import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.dto.oauth2.TokenInfo;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final TokenService tokenService;
    private final CookieService cookieService;

    public void setAuthenticationTokens(Map<String, Object> claims, HttpServletResponse response) {

        TokenInfo tokenInfo = tokenService.generateTokens(claims);
        claims.put("accessToken", tokenInfo.getAccessToken());
        cookieService.setCookie(response, claims, tokenInfo.getAccessToken());

    }

    public void setAuthenticationTokens(UserDTO userDTO, HttpServletResponse response) {
        Map<String, Object> claims = userDTO.getClaim();
        setAuthenticationTokens(claims, response);
    }

}
