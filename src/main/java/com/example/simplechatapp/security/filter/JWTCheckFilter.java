package com.example.simplechatapp.security.filter;

import com.example.simplechatapp.util.CustomJWTException;
import com.example.simplechatapp.util.JWTUtil;
import com.google.gson.Gson;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@Log4j2
public class JWTCheckFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        log.info("..... Check URI ..... " + path);

        return true;

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


        log.info(".....JWT Check FIlter Start.....");

        String authHeaderStr = request.getHeader("Authorization");
        String accessToken = authHeaderStr.substring(7);

        try {

            Map<String, Object> claims = JWTUtil.validToken(accessToken);

        } catch (CustomJWTException e) {

            log.error(".....JWT Check ERROR .....");
            log.error(e.getMessage());


            Gson gson = new Gson();

            String msg = gson.toJson(Map.of("error", "ERROR_ACCESS_TOKEN"));
            response.setContentType("application/json");
            PrintWriter printWriter = response.getWriter();

            printWriter.println(msg);
            printWriter.close();

        }

    }
}
