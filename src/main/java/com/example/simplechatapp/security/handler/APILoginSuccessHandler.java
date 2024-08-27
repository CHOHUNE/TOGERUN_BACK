package com.example.simplechatapp.security.handler;

import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.repository.RefreshTokenRepository;
import com.example.simplechatapp.util.JWTUtil;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;


@RequiredArgsConstructor
public class APILoginSuccessHandler implements org.springframework.security.web.authentication.AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(APILoginSuccessHandler.class);
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;



    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        UserDTO userDTO = (UserDTO) authentication.getPrincipal();

        //authentication 의 principal 은 userDetail 에서 loadByUserName 에서 리턴한 값이다.

        Map<String, Object> claims = userDTO.getClaim();
        log.info("normal claims : {}", claims);

        String accessToken = jwtUtil.generateAccessToken(claims, 10);
        String refreshToken= jwtUtil.generateRefreshToken(claims, 60 * 24);

        String email = claims.get("email").toString();
        refreshTokenRepository.saveRefreshToken(email, refreshToken, 60 * 24 * 60 * 1000);

        claims.put("accessToken", accessToken);
//        claims.put("refreshToken", refreshToken);

        Gson gson = new Gson();

        String jsonStr = gson.toJson(claims);

        response.setContentType("application/json;charset=UTF-8");

        PrintWriter printWriter = response.getWriter();
        printWriter.println(jsonStr);
        printWriter.close();

    }
}
