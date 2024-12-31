package com.example.simplechatapp.service;


import com.example.simplechatapp.dto.oauth2.TokenInfo;
import com.example.simplechatapp.repository.RefreshTokenRepository;
import com.example.simplechatapp.util.CustomJWTException;
import com.example.simplechatapp.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    private static final long REFRESH_TOKEN_EXPIRATION_TIME = 60 * 24 * 60 * 1000L;



    public TokenInfo generateTokens(Map<String, Object> claims) {

        String accessToken = jwtUtil.generateAccessToken(claims, 10);
        String refreshToken = jwtUtil.generateRefreshToken(claims, 60 * 24);

        String email = claims.get("email").toString();
        refreshTokenRepository.saveRefreshToken(email, refreshToken, REFRESH_TOKEN_EXPIRATION_TIME);

        return new TokenInfo(accessToken, refreshToken);
    }



    public TokenInfo refreshTokens(String accessToken) {

        Map<String,Object> claims = jwtUtil.getClaims(accessToken);
        String email = claims.get("email").toString();

        String storedRefreshToken = refreshTokenRepository.getRefreshToken(email);

        if (storedRefreshToken == null) {
            throw new CustomJWTException("INVALID_REFRESH_TOKEN");
        }

        validateRefreshToken(storedRefreshToken, email);

        String newAccessToken = jwtUtil.generateAccessToken(claims, 10);
        String newRefreshToken = storedRefreshToken;

        if (shouldRefreshToken(storedRefreshToken)) {
            newRefreshToken = jwtUtil.generateRefreshToken(claims, 60 * 24);
            refreshTokenRepository.saveRefreshToken(email, newRefreshToken, REFRESH_TOKEN_EXPIRATION_TIME);

        }
        return new TokenInfo(newAccessToken, newRefreshToken);
    }



    public void validateRefreshToken(String refreshToken, String email) {

        try {
            jwtUtil.validToken(refreshToken);
        } catch (CustomJWTException e) {
            refreshTokenRepository.deleteRefreshToken(email);
            throw new CustomJWTException("Invalid refresh token");
        }
    }



    public boolean shouldRefreshToken(String token) {
        try{
            Map<String,Object> claims= jwtUtil.validToken(token);
            Number expNumber =(Number) claims.get("exp");
            // Number : 추상 클래스로, 숫자형 데이터를 다루는 클래스의 최상위 클래스

            long exp = expNumber.longValue();
            long currentTimeInSeconds = System.currentTimeMillis() / 1000;

            return (exp - currentTimeInSeconds) < (12 * 60 * 60); // 12시간 남은 경우 리프레쉬 토큰 갱신

        }catch(CustomJWTException e){
            return true;
        }
    }

}
