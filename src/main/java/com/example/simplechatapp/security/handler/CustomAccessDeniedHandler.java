package com.example.simplechatapp.security.handler;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class CustomAccessDeniedHandler implements AccessDeniedHandler{

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {

        Gson gson = new Gson();

        String jsonStr = gson.toJson(Map.of("error", "Access Denied"));
        response.setContentType("application/json;charset=UTF-8");

        PrintWriter printWriter = response.getWriter();
        printWriter.println(jsonStr);

        printWriter.close();

        // Gson을 이용해 json 형태로 에러메세지 생성후 response 에 담아 보낸다.
        //

    }
}
