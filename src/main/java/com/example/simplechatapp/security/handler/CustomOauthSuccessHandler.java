package com.example.simplechatapp.security.handler;

import com.example.simplechatapp.dto.oauth2.CustomOAuth2User;
import com.example.simplechatapp.repository.RefreshTokenRepository;
import com.example.simplechatapp.util.JWTUtil;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;


@Component
@Log4j2
@RequiredArgsConstructor
public class CustomOauthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final RefreshTokenRepository refreshTokenRepository;

    private final JWTUtil jwtUtil;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        Map<String, Object> claims = customUserDetails.getClaim();


        log.info("oauth claims : {}", claims);

        String accessToken = jwtUtil.generateAccessToken(claims, 10);
        String refreshToken = jwtUtil.generateRefreshToken(claims, 60 * 24);

        claims.put("accessToken", accessToken);
//        claims.put("refreshToken", refreshToken);

        String email = claims.get("email").toString();
        refreshTokenRepository.saveRefreshToken(email, refreshToken, 60 * 24 * 60 * 1000);

        Gson gson = new Gson();

        String jsonStr = gson.toJson(claims);


        response.setContentType("application/json;charset=UTF-8");


        String encodedJsonStr = URLEncoder.encode(jsonStr, StandardCharsets.UTF_8);
        Cookie cookie = new Cookie("member", encodedJsonStr);
        cookie.setMaxAge(60 * 60 * 60);
        cookie.setPath("/");
//        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        response.sendRedirect("http://localhost:3000/");
        request.getSession().invalidate();

        PrintWriter printWriter = response.getWriter(); // response 에 json 형태로 claims 를 담아 보낸다.
        printWriter.println(jsonStr);
        printWriter.close();


    }
}
