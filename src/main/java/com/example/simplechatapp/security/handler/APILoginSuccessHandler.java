package com.example.simplechatapp.security.handler;

import com.example.simplechatapp.dto.UserDTO;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class APILoginSuccessHandler implements org.springframework.security.web.authentication.AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        UserDTO userDTO = (UserDTO) authentication.getPrincipal();

        //authentication 의 principal 은 userDetail 에서 loadByUserName 에서 리턴한 값이다.

        Map<String, Object> claims = userDTO.getClaim();

        claims.put("accessToken", "");
        claims.put("refreshToken", "");

        Gson gson = new Gson();

        String jsonStr = gson.toJson(claims);

        response.setContentType("application/json;charset=UTF-8");

        PrintWriter printWriter = response.getWriter();
        printWriter.println(jsonStr);
        printWriter.close();






    }
}
