package com.example.simplechatapp.controller.intercepter;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.util.Map;

public class CustomHandshakeInterceptor implements HandshakeInterceptor {

// JWT 를 가져올 것인가? Security Authentication 의 principal 을 가져올 것인가?
    // JWT : 유연성, 무상태 ( STATELESS)
    // principal : 시큐리티와의 통합성

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        Principal principal = request.getPrincipal();

        if (principal != null) {

            attributes.put("username", principal.getName());

            return true;
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
