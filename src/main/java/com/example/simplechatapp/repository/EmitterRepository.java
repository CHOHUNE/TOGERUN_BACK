package com.example.simplechatapp.repository;


import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;



public interface EmitterRepository {

    SseEmitter save(String emitterId, SseEmitter sseEmitter); // emitterId 와 sseEmitter 를 사용하여 SSE 이벤트 전송 객체를 저장

    void saveEventCache(String emitterId, Object event); //  이벤트 캐시 아이디와 이벤트 객체를 저장
    Map<String,SseEmitter> findAllEmitterStartWithByUserNickname(String userNickname); // 주어진 memberId 로 시작하는 모든 emitter 를 가져옴
    Map<String,Object> findAllEventCacheStartWithByUserNickname(String userNickname); // 주어진 memberId 로 시작하는 모든 Event 를 가져옴

    void deleteById(String id); //

}
