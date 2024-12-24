package com.example.simplechatapp.security.filter;

import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.dto.oauth2.CustomOAuth2User;
import com.example.simplechatapp.dto.oauth2.TokenResponse;
import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.entity.UserRole;
import com.example.simplechatapp.repository.UserRepository;
import com.example.simplechatapp.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AutoLoginFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
//    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 메인 페이지(/)이고 인증되지 않은 상태일 때만 자동 로그인 실행
        if (request.getRequestURI().equals("/") ) {

            // ID 2번 유저로 자동 로그인
            User dummyUser = userRepository.findById(2L)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // UserDTO 생성
            UserDTO userDTO = new UserDTO(
                    dummyUser.getId(),
                    dummyUser.getEmail(),
                    "",
                    dummyUser.getName(),
                    dummyUser.getNickname(),
                    dummyUser.isSocial(),
                    dummyUser.getGender(),
                    dummyUser.getAge(),
                    dummyUser.getMobile(),
                    dummyUser.getImg(),
                    dummyUser.getUserRoleList().stream()
                            .map(UserRole::name)
                            .collect(Collectors.toList()),
                    dummyUser.isDeleted(),
                    dummyUser.getDeletedAt()
            );

            // JWT 토큰 생성 및 설정
            Map<String, Object> claims = userDTO.getClaim();

            TokenResponse tokens = jwtUtil.createTokens(claims);

//            String accessToken = jwtUtil.generateAccessToken(claims, 10);
//            String refreshToken = jwtUtil.generateRefreshToken(claims, 60 * 24);
//
//            // Refresh 토큰 저장
//            refreshTokenRepository.saveRefreshToken(dummyUser.getEmail(), refreshToken, 60 * 24 * 60 * 1000);

            // Authentication 객체 생성 및 설정
            CustomOAuth2User customOAuth2User = new CustomOAuth2User(userDTO);
            OAuth2AuthenticationToken auth = new OAuth2AuthenticationToken(
                    customOAuth2User,
                    customOAuth2User.getAuthorities(),
                    "google"


            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            // JWT 토큰을 응답 헤더에 추가
            response.setHeader("Authorization", "Bearer " + tokens.getAccessToken());
        }

        filterChain.doFilter(request, response);
    }
}