package com.example.simplechatapp.security.handler;

import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.dto.oauth2.CustomOAuth2User;
import com.example.simplechatapp.repository.RefreshTokenRepository;
import com.example.simplechatapp.service.AuthenticationService;
import com.example.simplechatapp.util.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;


@Component
@Log4j2
@RequiredArgsConstructor
public class CustomOauthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JWTUtil jwtUtil;
    private final AuthenticationService authenticationService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        UserDTO userDTO;

        if (authentication.getPrincipal() instanceof CustomOAuth2User) {

            CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

            userDTO = oAuth2User.getUserDTO();
        }else{
            userDTO = (UserDTO) authentication.getPrincipal();
        }

        Map<String, Object> claims = userDTO.getClaim();

        authenticationService.setAuthenticationTokens(claims, response);

        String redirectUrl = determineRedirectUrl(claims);
        response.sendRedirect(redirectUrl);

    }

    private String determineRedirectUrl(Map<String, Object>claims) {

        Boolean isSocial = (Boolean) claims.get("social");
        Boolean isDeleted =(Boolean) claims.get("isDeleted");

        if (Boolean.TRUE.equals(isSocial)) {
            return "https://www.togerun.shop/member/modify/";

        } else if (Boolean.TRUE.equals(isDeleted)) {
            return "https://www.togerun.shop/member/restore/" + claims.get("id");

        } else {
            return "https://www.togerun.shop/";
        }
    }
}