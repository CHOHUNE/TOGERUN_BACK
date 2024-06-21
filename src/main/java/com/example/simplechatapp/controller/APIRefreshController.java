package com.example.simplechatapp.controller;


import com.example.simplechatapp.util.CustomJWTException;
import com.example.simplechatapp.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

@RestController
@Log4j2
@RequiredArgsConstructor
public class APIRefreshController {

    @RequestMapping("/api/member/refresh")
    public Map<String, Object> refresh(@RequestHeader("Authorization") String authHeader, String refreshToken) throws CustomJWTException {

        if (refreshToken == null) {
            throw new CustomJWTException("NULL_REFRESH");
        }

        if (authHeader == null) {
            throw new CustomJWTException("INVALID_AUTHORIZATION_HEADER");
        }
        String accessToken = authHeader.substring(7);

        if (!checkExpiredToken(accessToken)) {
            return Map.of("accessToken",accessToken,"refreshToken",refreshToken);

        }

        Map<String,Object> claims = JWTUtil.validToken(refreshToken);

        log.info ("refresh claims{}",claims);

        String newAccessToken = JWTUtil.generationToken(claims, 10);
        String newRefreshToken = checkTime((Integer)claims.get("exp"))?JWTUtil.generationToken(claims, 60*24):refreshToken;

        return Map.of("accessToken",newAccessToken,"refreshToken",newRefreshToken);

    }

    private boolean checkTime(Integer exp) {
        Date expireDate = new Date((long) exp * (1000));

        long gap = expireDate.getTime() - System.currentTimeMillis();

        long leftMin = gap/(1000*60); // 1000ms = 1s, 60s = 1m

        return leftMin < 60; // 60분 이하로 남았을 때 리프레시

    }

    private boolean checkExpiredToken(String token) throws CustomJWTException {

        try {
            JWTUtil.validToken(token);

        } catch (CustomJWTException e) {
            if(e.getMessage().equals("EXPIRED")) {
                return true;
            }
        }

        return false;
    }
}
