package com.example.simplechatapp.security.filter;

import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.util.CustomJWTException;
import com.example.simplechatapp.util.JWTUtil;
import com.google.gson.Gson;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@Log4j2
public class JWTCheckFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        log.info("..... Check URI ..... " + path);

//        if (path.startsWith("/api/member/login") || path.startsWith("/api/member/refresh)")) {
//            return true;
//        }


        return true;

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


        log.info(".....JWT Check FIlter Start.....");

        String authHeaderStr = request.getHeader("Authorization");

        try {

            String accessToken = authHeaderStr.substring(7);
            Map<String,Object> claims = JWTUtil.validToken(accessToken);
            //accessToken 을 바탕으로 Jwt 토큰을 parserBuilder 실패시 catch 예외 처리
            // 성공시 return

            log.info("JWT claims{}", claims);

            String email=(String)claims.get("email");
            String pw = (String)claims.get("pw");
            String nickname=(String)claims.get("nickname");

            Boolean social = (Boolean) claims.get("social");
            List<String> roleNames = (List<String>) claims.get("roleNames");

            UserDTO userDTO = new UserDTO(email, pw, nickname, social.booleanValue(), roleNames);
            log.info(".....JWT Check Success.....");
            log.info("userDTO{}", userDTO);
            log.info(userDTO.getAuthorities());

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDTO, userDTO, userDTO.getAuthorities());
            // 토큰의 파라메타는 principal 과 credential (접근하려는 대상, 권한 )


            filterChain.doFilter(request, response);


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
