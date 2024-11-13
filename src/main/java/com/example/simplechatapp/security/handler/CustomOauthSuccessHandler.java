package com.example.simplechatapp.security.handler;

import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.dto.oauth2.CustomOAuth2User;
import com.example.simplechatapp.service.AuthenticationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;


@Component
@Log4j2
@RequiredArgsConstructor
public class CustomOauthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthenticationService authenticationService;


    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        UserDTO userDTO = oAuth2User.getUserDTO();
        Map<String, Object> claims = userDTO.getClaim();

        authenticationService.setAuthenticationTokens(claims, response);
        response.sendRedirect(determineRedirectUrl(claims));

    }

    private String determineRedirectUrl(Map<String, Object> claims) {

        Boolean isSocial = (Boolean) claims.get("social");
        Boolean isDeleted = (Boolean) claims.get("isDeleted");

        if (Boolean.TRUE.equals(isSocial)) {

            log.info("Redirect : social user");
            return frontendUrl + "/member/modify/";

        } else if (Boolean.TRUE.equals(isDeleted)) {
            log.info("Redirect : deleted user");
            return frontendUrl + "/member/restore/" + claims.get("id");

        } else {
            return frontendUrl + "/";
        }
    }
}