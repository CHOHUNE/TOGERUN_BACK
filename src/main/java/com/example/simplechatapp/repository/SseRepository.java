package com.example.simplechatapp.repository;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

public interface SseRepository {

    SseEmitter save(String id, SseEmitter emitter);

    void saveEventCache(String eventCacheId, Object event);

    Map<String, SseEmitter> findAllEmitterStartsWithUserNickname(String userNickname);
    Map<String, Object> findAllEventCacheStartsWithUserNickname(String userNickname);

    void deleteEmitterById(String id);

    void deleteAllEmitterStartWithId(String id);

    void deleteAllEventCacheStartsWithId(String id);
}


