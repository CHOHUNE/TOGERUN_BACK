package com.example.simplechatapp.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // WebSocketMessageBorkerConfigurer 인터페이스를 구현하여 WebSocket 관련 설정을 할 수 있다.



    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {

        config.enableSimpleBroker("/topic");
        // 메모리 내에 simple 메시지 브로커를 활성화하고, /topic 으로 시작하는 메세지를 브로커가 처리하도록 합니다.
        // 주로 구독에 사용된다.


        config.setApplicationDestinationPrefixes("/app");
        // app: 클라이언트가 메시지를 송신할 때 사용할 prefix
        // app/chat 으로 메세지를 보내면 서버의 컨트롤러로 라우팅 된다.

    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:8080", "http://localhost:3000")
                .withSockJS();

         // use allowedOriginPatterns here
        //클라이언트가 /ws endpoint로 소켓 연결을 시도할 수 있도록 한다.
        // "*"은 모든 도메인에서 접근을 허용한다.
        // withSockJS()는 SockJS를 사용할 수 있도록 한다.
        // SockJS는 WebSocket을 지원하지 않는 브라우저에서도 WebSocket을 사용할 수 있도록 한다.

    }
}
