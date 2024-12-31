package com.example.simplechatapp.service;

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
public class CookieService {

    public void setCookie(HttpServletResponse response, Map<String, Object> claims,String accessToken) {

        Gson gson = new Gson();
        String jsonStr = gson.toJson(claims);

        String encodedJsonStr = URLEncoder.encode(jsonStr, StandardCharsets.UTF_8);

        Cookie cookie = createCookie("member", encodedJsonStr);

        response.addCookie(cookie);
        response.setHeader("Authorization", "Bearer " + claims.get("accessToken"));
    }

    private Cookie createCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(6 * 60 * 60); // 6시간
        cookie.setPath("/");
        cookie.setSecure(true);
        cookie.setHttpOnly(false);
        cookie.setDomain("togerun.shop");
        return cookie;
    }

}
