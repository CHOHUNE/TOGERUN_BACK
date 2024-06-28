package com.example.simplechatapp.config;


import com.example.simplechatapp.security.filter.JWTCheckFilter;
import com.example.simplechatapp.security.handler.APILoginFailureHandler;
import com.example.simplechatapp.security.handler.APILoginSuccessHandler;
import com.example.simplechatapp.security.handler.CustomAccessDeniedHandler;
import com.example.simplechatapp.security.CustomOAuth2UserService;
import com.example.simplechatapp.security.handler.CustomOauthSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

@Configuration
@Log4j2
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class CustomSecurityConfig {



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomOAuth2UserService customOAuth2UserService, CustomOauthSuccessHandler customOauthSuccessHandler) throws Exception {
        log.info("-----------------security config---------------------");

//        http.cors(httpSecurityCorsConfigurer -> {
//            httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource());
//        });

        http.httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(httpSecuritySessionManagementConfigurer ->
                        httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.NEVER))
                .cors(corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {
                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        CorsConfiguration configuration = new CorsConfiguration();

                        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
                        configuration.setAllowedMethods(Collections.singletonList("*"));
                        configuration.setAllowCredentials(true);
                        configuration.setAllowedHeaders(Collections.singletonList("*"));
                        configuration.setMaxAge(3600L);

                        configuration.setExposedHeaders(Collections.singletonList("Set-Cookie"));
                        configuration.setExposedHeaders(Collections.singletonList("Authorization"));

                        return configuration;
                    }
                }));


        http.formLogin(config -> {
            config.loginPage("/api/member/login");
            config.successHandler(new APILoginSuccessHandler());
            config.failureHandler(new APILoginFailureHandler());
        });

        http.oauth2Login((oauth) -> oauth.userInfoEndpoint(
                                (userInfoEndpointConfig) -> userInfoEndpointConfig //userInfoEndpointConfig : 사용자 정보를 가져오는 엔드포인트를 구성하는 빌더
                                        .userService(customOAuth2UserService))
                        .successHandler(customOauthSuccessHandler))
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/oauth2/authorization/kakao", "/login", "/error").permitAll() // Explicitly permit OAuth2 login and error page
                        .anyRequest().authenticated()
                );

        http.logout(logout -> logout
                .logoutUrl("/api/member/logout") // Endpoint to trigger logout
                .invalidateHttpSession(true) // Invalidate session
                .clearAuthentication(true) // Clear authentication
                .deleteCookies("JSESSIONID", "member") // Specify cookies to delete
                .logoutSuccessUrl("http://localhost:3000/") // Redirect after logout
        );


//  requestMatchers 부분에 소셜로그인 링크 추가

//        http.addFilterBefore(new JWTCheckFilter(), UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(new JWTCheckFilter(), OAuth2LoginAuthenticationFilter.class);

        http.exceptionHandling(httpSecurityExceptionHandlingConfigurer -> {
            httpSecurityExceptionHandlingConfigurer.accessDeniedHandler(new CustomAccessDeniedHandler());
        });



        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }




}
