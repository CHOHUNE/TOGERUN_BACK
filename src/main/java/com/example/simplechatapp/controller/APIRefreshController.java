package com.example.simplechatapp.controller;


import com.example.simplechatapp.dto.oauth2.TokenInfo;
import com.example.simplechatapp.service.TokenService;
import com.example.simplechatapp.util.CustomJWTException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
@RequiredArgsConstructor
public class APIRefreshController {

    private final TokenService tokenService;

    @RequestMapping("/api/member/refresh")
    public void refresh(@RequestHeader("Authorization") String authHeader) throws CustomJWTException {


        log.info("Refresh Token Request Received");


        if (authHeader == null || authHeader.length() < 7) {
            throw new CustomJWTException("INVALID_STRING");
        }

        String accessToken = authHeader.substring(7);
        TokenInfo tokenInfo = tokenService.refreshTokens(accessToken);


        log.info("Token Refreshed Successfully : {}", tokenInfo);

    }
}
