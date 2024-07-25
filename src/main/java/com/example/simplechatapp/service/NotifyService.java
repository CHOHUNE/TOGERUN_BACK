package com.example.simplechatapp.service;

import com.example.simplechatapp.dto.NotifyDto;
import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.entity.NotificationType;
import com.example.simplechatapp.entity.Notify;
import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.repository.EmitterRepository;
import com.example.simplechatapp.repository.NotifyRepository;
import com.example.simplechatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@Log4j2
@RequiredArgsConstructor
public class NotifyService {


    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    private final EmitterRepository emitterRepository;
    private final NotifyRepository notifyRepository;
    private final UserRepository userRepository;

    public SseEmitter subscribe(String userNickname, String lastEventId) {

        String emitterId = makeTimeIncludeId(userNickname);
        SseEmitter sseEmitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

        sseEmitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
        sseEmitter.onError((e) -> emitterRepository.deleteById(emitterId));
        sseEmitter.onTimeout(() -> emitterRepository.deleteById(emitterId));

        //503 에러 방지를 위한 더미 이벤트 전송

        String eventId =makeTimeIncludeId(userNickname);
        sendNotification(sseEmitter, eventId, emitterId, "EventStream Created. [userEmail=" + userNickname + "]");

        // 클라이언트가 미수신한 Event 목록이 존재할 경우 전송하여 Event 유실을 예방

        if(hasLostData(lastEventId)){
            sendLostData(lastEventId,userNickname,emitterId,sseEmitter);
        }

        return sseEmitter;

    }

    // userNickname 과 lastEventId를 받아 구독을 서정 하고 emitterId 생성 -> makeTimeIncludeId 메소드를 통해 고유한 아이디 생성 ->
    // SsEmitter 객체 생성/ 저장  -> onCompletion, onError, onTimeout 메소드를 통해 에러 발생시 삭제 -> 생성된 SseEmitter 클라이언트에게 이벤트 전송

    // makeTimeIncluded 한 브라우저에서 여러개의 구독을 진행할 때 탭 마다 SseEmitter 의 구분을 위해 시간을 붙여 구분할 수 있어야 함
    // Last-Event-Id 로 마지막 받은 전송 이벤트 ID 가 무엇인지 알고, 받지 못한 데이터 정보들에 대해 인지할 수 있어야 함
    // 등록 후 SseEmitter 의 유효 시간 동안 데이터가 전송되지 ㅇ낳으면 503 에러 발생 - > 맨 처음 연결 진행 더미데이터를 보내 이를 방지

    private String makeTimeIncludeId(String email) {

        return email+"_"+System.currentTimeMillis(); //고유한 아이디를 만들기 위해 이메일과 현재 시간을 합침
    }

    private void sendNotification(SseEmitter emitter, String eventId, String emitterId, Object data) {

        try {
            emitter.send(SseEmitter.event()
                    .id(eventId)
                    .name("sse")
                    .data(data)
            );

        } catch (IOException e) {
            emitterRepository.deleteById(emitterId);
        }
    }

    //SseEmitter 를 통해 이벤트를 전송하는 메서드
    // 파라메터  : SsEmitter 객체인 emitter, eventId, emitterId ( 식별을 위한 고유 ID ) , data ( 전송할 데이터 )


    private boolean hasLostData(String lastEventId) {
        return !lastEventId.isEmpty();
    }
    // lastEventId 가 비어있지 않다 -> controller 의 헤더를 통해 lastEventId 가 들어 왔다 - > 손실된 이벤트가 있다 -> true
    // lastEventId 가 비어있다 -> false

    private void sendLostData(String lastEventId, String userEmail, String emitterId, SseEmitter emitter) {
        Map<String ,Object> eventCaches = emitterRepository.findAllEventCacheStartWithByUserNickname(userEmail);

        eventCaches.entrySet().stream()
                .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                .forEach(entry -> sendNotification(emitter, entry.getKey(), emitterId, entry.getValue()));

    }
// 구독자의 이메일을 기반으로 이벤트 캐시를 가져와 마지막 이벤트 ID 와 비교하여 미수신한 데이터 전송



    public void send(String receiver, NotificationType notificationType, String content, String url) {

        Notify notification = notifyRepository.save(createNotification(receiver, notificationType, content, url));



        String eventId = receiver + "_" + System.currentTimeMillis();

        Map<String,SseEmitter> emitters = emitterRepository.findAllEmitterStartWithByUserNickname(receiver);

        emitters.forEach(
                (key,emitter) ->{
                    emitterRepository.saveEventCache(key,notification);
                    sendNotification(emitter,eventId,key, NotifyDto.Response.createResponse(notification));
                }
        );
    }

    // 지정된 수신자에게 알림을 전송하는 메서드 : 받아온 정보를 저장한 후 Notify 객체를 생성
    // findAllEmitterStartWithByMemberId(receiverEmail) 을 통해 수신자 이메일로 시작하는 모든 SseEmitter 객체를 가져옴
    // 각 Ssemitter 에 대해 이벤트 캐시에 key 와 생성한 Notify 객체를 저장하고,
    // SendNotification 메서드를 호출해 알림과 관련된 데이터 (eventId, key, ResponseNotifyDto) 를 emitter 로 전송

    private Notify createNotification(String receiver, NotificationType notificationType, String content, String url) {

        User user = userRepository.findByEmail(receiver);

        return Notify.builder()
                .createdAt(LocalDateTime.now())
                .receiver(user)
                .notificationType(notificationType)
                .content(content)
                .url(url)
                .isRead(false)
                .build();
    }

}
