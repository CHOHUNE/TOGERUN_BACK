package com.example.simplechatapp.security.handler;

import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.entity.UserRole;
import com.example.simplechatapp.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.debug("CustomAccessDeniedHandler.handle() 메서드 시작");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ErrorResponse errorResponse = null;

        if (authentication != null && authentication.isAuthenticated()) {
            log.debug("인증된 사용자: {}", authentication.getName());
            User user = userRepository.getWithRole(authentication.getName());

            if (user.getUserRoleList().contains(UserRole.ROLE_BRONZE)) {
                errorResponse = ErrorResponse.builder()
                        .status(HttpStatus.FORBIDDEN)
                        .message("프로필 업데이트가 필요합니다.")
                        .redirect("/member/modify")
                        .errorStatus("NEED_PROFILE_UPDATE")
                        .build();

                log.info("BRONZE 등급 사용자 접근 거부: {}", errorResponse.getMessage());
            }
        }

        sendJsonResponse(response, errorResponse);
        log.debug("CustomAccessDeniedHandler.handle() 메서드 종료");
    }

    private void sendJsonResponse(HttpServletResponse response, ErrorResponse errorResponse) throws IOException {
        response.setStatus(errorResponse.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        log.debug("JSON 응답 전송 완료: {}", errorResponse);
    }
}