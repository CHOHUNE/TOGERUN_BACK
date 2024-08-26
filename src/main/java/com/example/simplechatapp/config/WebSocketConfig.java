package com.example.simplechatapp.config;


import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Log4j2
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // WebSocketMessageBorkerConfigurer 인터페이스를 구현하여 WebSocket 관련 설정을 할 수 있다.

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.database}")
    private int redisDatabase;


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) { //클라이언트에서 WebSocket을 사용할 수 있는 엔드포인트
        registry.addEndpoint("/chat") // ws://localhost:8080/chat
                .setAllowedOriginPatterns("*"); // 일반 HTTP 요청과 별도로 @RequestMapping 경로와는 별도로 취급 되어 api 를 따로 붙일 필요가 없다.
        // "*"은 모든 도메인에서 접근을 허용한다.
    }

    // withSockJS()는 SockJS를 사용할 수 있도록 한다.
    // SockJS는 WebSocket을 지원하지 않는 브라우저에서도 WebSocket을 사용할 수 있도록 한다.


    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) { // 메시지를 중간에 라우팅 하는 역할 -브로커


        log.info("Configuring message broker{}", registry);

        registry.enableSimpleBroker("/topic");
//                .setRelayHost(redisHost)
//                .setRelayPort(redisPort)
//                .setVirtualHost(String.valueOf(redisDatabase));
//                        .setSystemLogin("")
//                                .setSystemPasscode("")

        // 메모리 내에 simple 메시지 브로커를 활성화하고,
        // /topic 으로 시작하는 메세지를 브로커가 구독 하도록 한다
        // /sub 로 많이 쓰인다 )


        registry.setApplicationDestinationPrefixes("/app"); //pub URL
        // app: 클라이언트가 메시지를 송신할 때 사용할 prefix
        // pub/chat 으로 메세지를 보내면 서버의 컨트롤러로 라우팅 된다.


    }

    @Bean
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

}
