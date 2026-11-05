package com.tukorea.bus.location.stream;

import com.tukorea.bus.location.domain.BusLocation;
import com.tukorea.bus.location.dto.FindLatestBusLocationResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 학생 앱에 위치를 푸시하는 SSE 구독 관리자. 특정 버스 하나 또는 전체 버스를 구독할 수 있다.
 * 클라이언트는 끊기면 다시 연결하고, 재연결 직후 현재 값을 한 번 받는다.
 */
@Slf4j
@Component
public class LocationStreamHub {

    public static final String EVENT_NAME = "location";
    private static final long TIMEOUT_MS = 30 * 60 * 1000L;

    private record Subscription(String busId, SseEmitter emitter) {

        boolean accepts(String id) {
            return busId == null || busId.equals(id);
        }
    }

    private final List<Subscription> subscriptions = new CopyOnWriteArrayList<>();

    /**
     * @param busId   null이면 모든 버스를 구독한다
     * @param initial 연결 직후 보낼 현재 위치들(없으면 빈 목록)
     */
    public SseEmitter subscribe(String busId, List<BusLocation> initial) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        Subscription subscription = new Subscription(busId, emitter);
        subscriptions.add(subscription);
        emitter.onCompletion(() -> subscriptions.remove(subscription));
        emitter.onTimeout(emitter::complete);
        emitter.onError(e -> subscriptions.remove(subscription));

        for (BusLocation location : initial) {
            send(subscription, location);
        }
        return emitter;
    }

    public void publish(BusLocation location) {
        for (Subscription subscription : subscriptions) {
            if (subscription.accepts(location.id())) {
                send(subscription, location);
            }
        }
    }

    /** 프록시/로드밸런서가 유휴 연결을 끊지 않도록 주기적으로 주석 이벤트를 보낸다. */
    @Scheduled(fixedDelay = 20_000)
    void heartbeat() {
        for (Subscription subscription : subscriptions) {
            try {
                subscription.emitter().send(SseEmitter.event().comment("keep-alive"));
            } catch (IOException | IllegalStateException e) {
                subscriptions.remove(subscription);
            }
        }
    }

    public int subscriberCount() {
        return subscriptions.size();
    }

    private void send(Subscription subscription, BusLocation location) {
        try {
            subscription.emitter().send(SseEmitter.event()
                    .name(EVENT_NAME)
                    .data(FindLatestBusLocationResponse.from(location)));
        } catch (IOException | IllegalStateException e) {
            subscriptions.remove(subscription);
            log.debug("끊어진 SSE 구독 제거: {}", e.getMessage());
        }
    }
}
