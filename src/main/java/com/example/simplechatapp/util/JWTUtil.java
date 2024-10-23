package com.example.simplechatapp.util;

import com.example.simplechatapp.repository.RefreshTokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JWTUtil {

//    @Value("${jwt.secret.key}")
    private final String key = "12345678901234567890123456789012";

    private final RefreshTokenRepository refreshTokenRepository;

    public String generateToken(Map<String, Object> claims, int min) {
        SecretKey secretKey = Keys.hmacShaKeyFor(this.key.getBytes(StandardCharsets.UTF_8));

        // LocalDateTime을 문자열로 변환
        if (claims.containsKey("deletedAt") && claims.get("deletedAt") instanceof LocalDateTime) {
            claims.put("deletedAt", claims.get("deletedAt").toString());
        }

        return Jwts.builder()
                .setHeader(Map.of("typ", "JWT"))
                .setClaims(claims)
                .setIssuedAt(Date.from(ZonedDateTime.now().toInstant()))
                .setExpiration(Date.from(ZonedDateTime.now().plusMinutes(min).toInstant()))
                .signWith(secretKey)
                .compact();
    }

    public Map<String, Object> validToken(String token) throws CustomJWTException {
        try {
            SecretKey key = Keys.hmacShaKeyFor(this.key.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // 문자열을 LocalDateTime으로 변환
            if (claims.containsKey("deletedAt") && claims.get("deletedAt") != null) {
                claims.put("deletedAt", LocalDateTime.parse(claims.get("deletedAt").toString()));
            }

            return claims;
        } catch (MalformedJwtException e) {
            throw new CustomJWTException("MalFormed");
        } catch (ExpiredJwtException e) {
            throw new CustomJWTException("Expired");
        } catch (InvalidClaimException e) {
            throw new CustomJWTException("Invalid");
        } catch (JwtException e) {
            throw new CustomJWTException("JWTError");
        } catch (Exception e) {
            throw new CustomJWTException("Error");
        }
    }

    public String generateAccessToken(Map<String, Object> claims, int min) {
        return generateToken(claims, min);
    }

    public String generateRefreshToken(Map<String, Object> claims, int min) {
        String refreshToken = generateToken(claims, min);
        String email = claims.get("email").toString();
        refreshTokenRepository.saveRefreshToken(email, refreshToken, min * 60 * 1000L);
        return refreshToken;
    }

    public boolean validRefreshToken(String email, String refreshToken) {
        String storedRefreshToken = refreshTokenRepository.getRefreshToken(email);
        return refreshToken.equals(storedRefreshToken);
    }

    public Map<String, Object> getClaims(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(this.key.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // 문자열을 LocalDateTime으로 변환
            if (claims.containsKey("deletedAt") && claims.get("deletedAt") != null) {
                claims.put("deletedAt", LocalDateTime.parse(claims.get("deletedAt").toString()));
            }

            return claims;
        } catch (ExpiredJwtException e) {
            Claims claims = e.getClaims();
            // 만료된 토큰에서도 deletedAt 처리
            if (claims.containsKey("deletedAt") && claims.get("deletedAt") != null) {
                claims.put("deletedAt", LocalDateTime.parse(claims.get("deletedAt").toString()));
            }
            return claims;
        }
    }
}