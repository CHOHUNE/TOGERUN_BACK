package com.example.simplechatapp.repository;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseRepository {

    SseEmitter save(String id, SseEmitter emitter);

//
//    Map<String, SseEmitter> findAllEmitterStartsWithUserNickname(String userNickname);
//    Map<String, Object> findAllEventCacheStartsWithUserNickname(String userNickname);
//
//    void saveEventCache(String eventCacheId, Object event);
//    void deleteEmitterById(String id);
//    void deleteAllEmitterStartWithId(String id);
//    void deleteAllEventCacheStartsWithId(String id);
}


