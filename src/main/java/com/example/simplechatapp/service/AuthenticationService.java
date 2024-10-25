package com.example.simplechatapp.service;

import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.repository.RefreshTokenRepository;
import com.example.simplechatapp.util.JWTUtil;
import com.google.gson.Gson;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {


    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public void setAuthenticationTokens(Map<String, Object> claims, HttpServletResponse response) {
        String accessToken = jwtUtil.generateAccessToken(claims, 10);
        String refreshToken = jwtUtil.generateRefreshToken(claims, 60 * 24);

        String email = (String) claims.get("email");
        refreshTokenRepository.saveRefreshToken(email, refreshToken, 60 * 24 * 60 * 1000);

        claims.put("accessToken", accessToken);

        setCookie(response, claims);
    }

    public void setAuthenticationTokens(UserDTO userDTO, HttpServletResponse response) {
        Map<String, Object> claims = userDTO.getClaim();
        setAuthenticationTokens(claims, response);
    }


    private void setCookie(HttpServletResponse response, Map<String, Object> claims) {

        Gson gson = new Gson();
        String jsonStr = gson.toJson(claims);

        String encodedJsonStr = URLEncoder.encode(jsonStr, StandardCharsets.UTF_8);

        Cookie cookie = new Cookie("member", encodedJsonStr);
        cookie.setMaxAge(60 * 60 * 60);

//        HTTPS 필수 설정 추가
        cookie.setSecure(true);
        cookie.setHttpOnly(false);


        cookie.setPath("/");
        response.addCookie(cookie);
    }

}
