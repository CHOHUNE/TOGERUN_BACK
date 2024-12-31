package com.example.simplechatapp.config;

import com.example.simplechatapp.dto.CommentResponseDto;
import com.example.simplechatapp.dto.PostDTO;
import com.example.simplechatapp.service.RedisSubscriber;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.lettuce.core.ReadFrom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {


    @Value("${spring.data.redis.host}")
    private String masterHost;

    @Value("${spring.data.redis.port}")
    private int masterPort;

//    @Value("${spring.data.redis.replica.host}")
//    private String replicaHost;
//
//    @Value("${spring.data.redis.replica.port}")
//    private int replicaPort;

    @Value("${spring.data.redis.password}")
    private String password;

    @Bean
    @Primary
    RedisConnectionFactory redisConnectionFactory() {

        // RedisStandaloneConfiguration : 단일 Redis 서버에 대한 정보를 저장하는 클래스
        RedisStandaloneConfiguration masterConfig = new RedisStandaloneConfiguration();
        masterConfig.setHostName(masterHost);
        masterConfig.setPort(masterPort);
        masterConfig.setPassword(password);

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .readFrom(ReadFrom.REPLICA_PREFERRED) // 읽기 전용 작업을 수행할 때, 우선적으로 읽기 전용 레플리카 노드를 사용하도록 설정
                .build();

        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(masterConfig, clientConfig);
        connectionFactory.afterPropertiesSet(); // 빈이 생성된 후에 추가적인 설정을 해주는 메소드


        return connectionFactory;

    }


    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mapper.addMixIn(PostDTO.class, TypeInfoMixin.class);
        mapper.addMixIn(CommentResponseDto.class, TypeInfoMixin.class);

        return mapper;
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @Primary
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withCacheConfiguration("member", defaultCacheConfig.entryTtl(Duration.ofMinutes(15)))
                .withCacheConfiguration("post", defaultCacheConfig.entryTtl(Duration.ofMinutes(15)))
                .withCacheConfiguration("chat", defaultCacheConfig.entryTtl(Duration.ofMinutes(15)))
                .withCacheConfiguration("view", defaultCacheConfig.entryTtl(Duration.ofMinutes(15)))
                .withCacheConfiguration("postComments", defaultCacheConfig.entryTtl(Duration.ofMinutes(15)))
                .withCacheConfiguration("userChatRooms", defaultCacheConfig.entryTtl(Duration.ofMinutes(15)))

                .build();
    }


    @Bean
    public MessageListenerAdapter messageListener(RedisSubscriber redisSubscriber) {
        return new MessageListenerAdapter(redisSubscriber, "onMessage");
        // onMessage 라는 메소드를 호출 -> 직접적으로 참조가 아닌 이름만 지정함
        // onMessage 에 직접적인 참조가 없는 이유 -> 옵저버 패턴과 유사한 이벤트 기반의 아키텍처 특성

    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory,
                                                                       MessageListenerAdapter listenerAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new ChannelTopic("chat.*"));

        return container;
    }
}
//Redis 서버와 상호작용 하기 위한 RedisTemplate 관련 설정을 해준다. Redis 서버에서는 bytes 코드만이
// 저장되므로 key 와 value 에 Serializer 를 설정 해준다. Json 포맷 형식으로 메세지를 교환하기 위해



