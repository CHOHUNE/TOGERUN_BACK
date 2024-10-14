package com.example.simplechatapp.controller;


import com.example.simplechatapp.repository.RefreshTokenRepository;
import com.example.simplechatapp.util.CustomJWTException;
import com.example.simplechatapp.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Log4j2
@RequiredArgsConstructor
public class APIRefreshController {

    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @RequestMapping("/api/member/refresh")
    public Map<String, Object> refresh(@RequestHeader("Authorization") String authHeader) throws CustomJWTException {


        log.info("Refresh Token Request Received");


        if (authHeader == null || authHeader.length() < 7) {
            throw new CustomJWTException("INVALID_STRING");
        }

        String accessToken = authHeader.substring(7);


        Map<String, Object> claims;

        try {
            claims = jwtUtil.validToken(accessToken);
        } catch (CustomJWTException e) {
            if (!"Expired".equals(e.getMessage())) {
                throw e;

            }
            claims = jwtUtil.getClaims(accessToken);
        }

        String email = claims.get("email").toString();

        // Redis 에서 Refresh Token 가져오기
        String storeRefreshToken = refreshTokenRepository.getRefreshToken(email);

        if (storeRefreshToken == null) {
            throw new CustomJWTException("INVALID_REFRESH_TOKEN");
        }

        try {
            jwtUtil.validToken(storeRefreshToken);
        } catch (CustomJWTException e) {
            refreshTokenRepository.deleteRefreshToken(email);
            throw new CustomJWTException("Invalid refresh token");
        }

        String newAccessToken = jwtUtil.generateAccessToken(claims, 10);
        String newRefreshToken = storeRefreshToken;

        if (shouldRefreshToken(storeRefreshToken)) {
            newRefreshToken = jwtUtil.generateRefreshToken(claims, 60 * 24);
            refreshTokenRepository.saveRefreshToken(email, newRefreshToken, 60 * 24 * 60 * 1000);

            log.info("New refresh token generated");
        }

        log.info("Token refreshed SuccessFully");

        return Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken);

    }


    private boolean shouldRefreshToken(String token) {

        try {
            Map<String, Object> claims = jwtUtil.validToken(token);
            Number expNumber = (Number) claims.get("exp");
            long exp = expNumber.longValue();
            long currentTimeInSeconds = System.currentTimeMillis() / 1000;

            return (exp - currentTimeInSeconds) < (12 * 60 * 60);

        } catch (CustomJWTException e) {
            return true;
        }
    }

}
