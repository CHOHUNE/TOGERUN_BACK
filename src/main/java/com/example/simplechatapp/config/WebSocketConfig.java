package com.example.simplechatapp.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

//@Log4j2
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) { //클라이언트에서 WebSocket을 사용할 수 있는 엔드포인트
        registry.addEndpoint("/chat") // ws://localhost:8080/chat
                .setAllowedOriginPatterns("*"); // 일반 HTTP 요청과 별도로 @RequestMapping 경로와는 별도로 취급 되어 api 를 따로 붙일 필요가 없다.
        // "*"은 모든 도메인에서 접근을 허용한다.
    }


    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) { // 메시지를 중간에 라우팅 하는 역할 -브로커



        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app"); //pub URL


    }
}
