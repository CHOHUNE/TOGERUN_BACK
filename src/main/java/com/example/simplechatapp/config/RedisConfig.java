package com.example.simplechatapp.config;

import com.example.simplechatapp.dto.CommentResponseDto;
import com.example.simplechatapp.dto.PostDTO;
import com.example.simplechatapp.service.RedisSubscriber;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
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

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mapper.addMixIn(PostDTO.class, TypeInfoMixin.class);
        mapper.addMixIn(CommentResponseDto.class, TypeInfoMixin.class);
//        mapper.addMixIn(UserDTO.class, TypeInfoMixin.class);

        return mapper;
    }

    @Bean
    @Qualifier("redisTemplate")
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
    @Primary // 0순위
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultCacheConfig)
//                .withCacheConfiguration("post", defaultCacheConfig.entryTtl(Duration.ofHours(1)))
                .withCacheConfiguration("member", defaultCacheConfig.entryTtl(Duration.ofMinutes(15)))
                .withCacheConfiguration("post", defaultCacheConfig.entryTtl(Duration.ofMinutes(15)))
                .withCacheConfiguration("chat", defaultCacheConfig.entryTtl(Duration.ofMinutes(15)))
                .withCacheConfiguration("view", defaultCacheConfig.entryTtl(Duration.ofMinutes(15)))
                .withCacheConfiguration("postComments", defaultCacheConfig.entryTtl(Duration.ofMinutes(15)))

                .build();
    }



    @Bean
    public MessageListenerAdapter messageListener(RedisSubscriber redisSubscriber) {
        return new MessageListenerAdapter(redisSubscriber,"onMessage");
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



