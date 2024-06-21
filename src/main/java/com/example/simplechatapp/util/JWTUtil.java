package com.example.simplechatapp.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

public class JWTUtil {

    private static  String key = "123213143258asdklsdjioasjivzcxiuhuihuivxcuihadsuidashiudasuu934589239";

    public static String generationToken(Map<String, Object> valueMap, int min) {

        SecretKey key = null;

        try{

            key = Keys.hmacShaKeyFor(JWTUtil.key.getBytes(StandardCharsets.UTF_8));


            //설명 : JWT 토큰을 생성하는 메소드
            // hmacShaKeyFor : 키를 생성하는 메소드
            // JWTUtil.key.getBytes(StandardCharsets.UTF_8) : key 값을 바이트로 변환하여 hmacShaKeyFor 메소드에 전달

        }catch(Exception e){


            throw new RuntimeException(e.getMessage());
        }

        String jwtStr = Jwts.builder()
                .setHeader(Map.of("type", "JWT"))
                .setClaims(valueMap)
                .setIssuedAt(Date.from(ZonedDateTime.now().toInstant())).
                setExpiration(Date.from(ZonedDateTime.now().plusMinutes(min).toInstant()))
                .signWith(key)
                .compact();

            //설명 : JWT 토큰을 생성하는 메소드
            // setHeader : JWT 헤더 설정
            // setClaims : JWT 클레임 설정 (토큰에 담을 정보)
            // setIssuedAt : 토큰 발급 시간 설정
            // signWith : 토큰 서명 설정 ( key : 서명에 사용할 비밀키)
            // compact : 토큰 생성


        return jwtStr;
    }

    public static Map<String, Object> validToken(String token) throws CustomJWTException {

        Map<String, Object> claims = null;


        try {

            SecretKey secretKey = Keys.hmacShaKeyFor(JWTUtil.key.getBytes(StandardCharsets.UTF_8));

            claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            // 설명 : JWT 토큰을 검증하는 메소드
            // setSigningKey : 서명에 사용할 비밀키 설정
            // parseClaimsJws : 토큰을 파싱하는 메소드 ( 파싱 : 토큰을 해석하여 정보를 추출하는 것)
            // getBody : 토큰의 정보를 추출하는 메소드 ( body : 토큰에 담긴 정보)

        } catch (MalformedJwtException e) {
            throw new CustomJWTException("MalFormed");

        } catch (ExpiredJwtException expiredJwtException) {
            throw new CustomJWTException("Expired");

        } catch (InvalidClaimException invalidClaimException) {
            throw new CustomJWTException("Invalid");

        } catch (JwtException jwtException) {
            throw new CustomJWTException("JWTError");

        } catch (Exception e) {
            throw new CustomJWTException("Error");
        }
        return claims;

    }
}
