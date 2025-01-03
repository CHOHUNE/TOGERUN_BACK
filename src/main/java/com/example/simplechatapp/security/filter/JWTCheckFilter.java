package com.example.simplechatapp.security.filter;

import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.util.CustomJWTException;
import com.example.simplechatapp.util.JWTUtil;
import com.google.gson.Gson;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@Log4j2
@RequiredArgsConstructor
public class JWTCheckFilter extends OncePerRequestFilter {


    private final JWTUtil jwtUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
//        String query = request.getQueryString();

        log.info(".....CHECK URI:{}....." , path);
        if (
                path.startsWith("/api/member") ||
            path.startsWith("/chat") ||
//            path.startsWith("/api/post/") ||
            path.startsWith("/authorization")||
            path.startsWith("/actuator")) {

            log.info(".....JWT Check Filter Skip.....");


            return true;
        }

        return false;

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


        log.info(".....JWT Check Filter Start.....");
        String authHeaderStr = request.getHeader("Authorization");

        try {
            String accessToken = authHeaderStr.substring(7);
            Map<String, Object> claims = jwtUtil.validToken(accessToken);
            //accessToken 을 바탕으로 Jwt 토큰을 parserBuilder 실패시 catch 예외 처리
            // 성공시 return

            log.info("JWT claims{}", claims);

//            Long id = (Long) claims.get("id");
            Long id = Long.valueOf(((Number) claims.get("id")).longValue());
            String email = (String) claims.get("email");
            String pw = (String) claims.get("pw");
            String nickname = (String) claims.get("nickname");
            Boolean social = (Boolean) claims.get("social");
            String name= (String) claims.get("name");
            String mobile = (String) claims.get("mobile");
            String gender = (String) claims.get("gender");
            String age = (String) claims.get("age");
            String img = (String) claims.get("img");

            List<String> roleNames = (List<String>) claims.get("roleNames");


            UserDTO userDTO = new UserDTO(id, email, pw, name,nickname,social.booleanValue(), gender,age,mobile,img,roleNames,false,null);
            // JWT claims 를 바탕으로 UserDTO 생성

            log.info(".....JWT Check Success.....");
            log.info("userDTO{}", userDTO);
            log.info(userDTO.getAuthorities());
            log.info(".....JWT Login Start.....");

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDTO, pw, userDTO.getAuthorities());
            // 토큰의 파라메타는 principal 과 credential (접근하려는 대상, 권한 )
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
//            }

            filterChain.doFilter(request, response);

        } catch (CustomJWTException e) {

            log.error(".....JWT Check ERROR .....");
            log.error(e.getMessage());

            Gson gson = new Gson();
            String msg = gson.toJson(Map.of("errorStatus", "ERROR_ACCESS_TOKEN"));

            response.setContentType("application/json");
            PrintWriter printWriter = response.getWriter();
            printWriter.println(msg);
            printWriter.close();

        }
    }
}
