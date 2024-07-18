package com.example.simplechatapp.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class EmitterRepositoryImpl implements EmitterRepository{

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>(); // key value 에 각각 emitterId 와 SseEmitter 객체를 저장
    private final Map<String, Object> eventCache = new ConcurrentHashMap<>();  // key value 에 각각 eventCacheId 와 eventCache 를 저장 한다

    // 코커렌트 해쉬맵을 쓰는 이유는 여러 클라이언트가 동시에 구독하고 이벤트를 전송할 수 있으므로 동시성 제어를 하는 것이 중요하기 때문



    @Override
    public SseEmitter save(String emitterId, SseEmitter sseEmitter) {
        emitters.put(emitterId, sseEmitter);

        return sseEmitter;
    }

    @Override
    public void saveEventCache(String eventCacheId, Object event) {

        eventCache.put(eventCacheId, event);

    }

    @Override
    public Map<String, SseEmitter> findAllEmitterStartWithByUserNickname(String userNickname) {
        return emitters.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(userNickname))
                .collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue));
    }

    @Override
    public Map<String, Object> findAllEventCacheStartWithByUserNickname(String userNickname) {
        return eventCache.entrySet().stream()
                .filter(entry->entry.getKey().startsWith(userNickname))
                .collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue)); // Entry : Map의 key와 value를 의미
    }

    @Override
    public void deleteById(String id) {

        emitters.remove(id);
    }

    @Override
    public void deleteAllEmitterStartWithId(String userId) {
        emitters.forEach(
                (key,emitter)->{
                    if(key.startsWith(userId)){
                        emitters.remove(key);
                    }
                }
        );

    }

    @Override
    public void deleteAllEventCacheStartWitId(String userId) {

        eventCache.forEach(
                (key,emitter)->{
                    if(key.startsWith(userId)){
                        eventCache.remove(key);
                    }
                }
        );
    }
}
