package com.example.simplechatapp.util;

import com.example.simplechatapp.repository.RefreshTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class JWTUtil {


    @Value("${jwt.secret.key}")
    private  String key ;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final RefreshTokenRepository refreshTokenRepository;

    public  String generateToken(Map<String, Object> claims, int min) {

        SecretKey secretKey;

        try{
            secretKey = Keys.hmacShaKeyFor(this.key.getBytes(StandardCharsets.UTF_8));


        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        return  Jwts.builder()
                .setHeader(Map.of("typ", "JWT"))
                .setClaims(claims)
                .setIssuedAt(Date.from(ZonedDateTime.now().toInstant()))
                .setExpiration(Date.from(ZonedDateTime.now().plusMinutes(min).toInstant()))
                .signWith(secretKey)
                .compact();
    }


    public  Map<String, Object> validToken(String token) throws CustomJWTException {
        Map<String, Object> claims = null;
        try{
            SecretKey key = Keys.hmacShaKeyFor(this.key.getBytes(StandardCharsets.UTF_8));
            claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token) // 파싱 및 검증, 실패 시 에러
                    .getBody();
        }catch(MalformedJwtException malformedJwtException){
            throw new CustomJWTException("MalFormed");
        }catch(ExpiredJwtException expiredJwtException){
            throw new CustomJWTException("Expired");
        }catch(InvalidClaimException invalidClaimException){
            throw new CustomJWTException("Invalid");
        }catch(JwtException jwtException){
            throw new CustomJWTException("JWTError");
        }catch(Exception e){
            throw new CustomJWTException("Error");
        }
        return claims;
    }

//    public static Map<String, Object> parseCookieValue(String cookieValue) {
//        try {
//            return objectMapper.readValue(cookieValue, Map.class);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to parse cookie value", e);
//        }
//    }

    public  String generateAccessToken(Map<String, Object> claims, int min) {

        return generateToken(claims, min);
    }

    public  String generateRefreshToken(Map<String, Object> claims, int min) {
        String refreshToken = generateToken(claims, min);
        String email = claims.get("email").toString();
        refreshTokenRepository.saveRefreshToken(email, refreshToken, min * 60 * 1000L);
        return refreshToken;
    }

    public boolean validRefreshToken(String email, String refreshToken) {

        String storedRefreshToken = refreshTokenRepository.getRefreshToken(email);
        return refreshToken.equals(storedRefreshToken);
    }

    public Map<String,Object> getClaims(String token) {

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(this.key.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims(); // 만료된 토큰에서도 클레임을 추출한다
        }
    }

}
