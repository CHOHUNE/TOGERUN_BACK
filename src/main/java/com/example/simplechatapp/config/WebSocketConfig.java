package com.example.simplechatapp.config;


import com.example.simplechatapp.controller.intercepter.CustomHandshakeInterceptor;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.security.Principal;
import java.util.Map;

@Log4j2
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // WebSocketMessageBorkerConfigurer 인터페이스를 구현하여 WebSocket 관련 설정을 할 수 있다.

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat") // SockJS 는 기본적으로 /info /websocket /xhr 등과 같은 경로를 사용하는데, 세그먼트가 3개 여야 한다. /ws/1/info 는 두개의 세그먼트로 보고 있다.
                .setAllowedOriginPatterns("*"); // 일반 HTTP 요청과 별도로 @RequestMapping 경로와는 별도로 취급 되어 api 를 따로 붙일 필요가 없다.
//        @EnableWebSocketMessageBroker 를 스면 스프링의 웹소켓 지원에 직접 사용된다

//                .setHandshakeHandler(new DefaultHandshakeHandler() {
//                    @Override
//                    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
//                        return request.getPrincipal();
//                    }
//                })

//                .addInterceptors(new CustomHandshakeInterceptor());
//                .withSockJS();
    }

    // use allowedOriginPatterns here
    //클라이언트가 /ws endpoint로 소켓 연결을 시도할 수 있도록 한다.
    // "*"은 모든 도메인에서 접근을 허용한다.
    // withSockJS()는 SockJS를 사용할 수 있도록 한다.
    // SockJS는 WebSocket을 지원하지 않는 브라우저에서도 WebSocket을 사용할 수 있도록 한다.


    @Override

    public void configureMessageBroker(MessageBrokerRegistry registry) {


        log.info("Configuring message broker{}", registry);

        registry.enableSimpleBroker("/topic"); //구독 URL
        // 메모리 내에 simple 메시지 브로커를 활성화하고, /topic 으로 시작하는 메세지를 브로커가 처리하도록 합니다.
        // 주로 구독에 사용된다.

        registry.setApplicationDestinationPrefixes("/app"); //prefix
        // app: 클라이언트가 메시지를 송신할 때 사용할 prefix
        // pub/chat 으로 메세지를 보내면 서버의 컨트롤러로 라우팅 된다.


    }


}
