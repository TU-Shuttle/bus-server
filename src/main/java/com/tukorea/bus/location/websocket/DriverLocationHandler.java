package com.tukorea.bus.location.websocket;

import com.tukorea.bus.location.dto.UpdateBusLocationRequest;
import com.tukorea.bus.location.service.InvalidLocationException;
import com.tukorea.bus.location.service.LocationService;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * 기사 앱이 보낸 위치({"id","lat","lng"})를 검증해 반영한다.
 * 잘못된 메시지는 연결을 끊지 않고 {"error":"..."} 로 알려준다.
 */
@Slf4j
@Component
@AllArgsConstructor
public class DriverLocationHandler extends TextWebSocketHandler {

    private final LocationService locationService;
    private final JsonMapper jsonMapper;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        try {
            UpdateBusLocationRequest request = jsonMapper.readValue(message.getPayload(),
                    UpdateBusLocationRequest.class);
            locationService.update(request.id(), request.lat(), request.lng(), request.accuracy(), request.status(),
                    request.direction());
        } catch (JacksonException e) {
            reject(session, "메시지 형식이 올바르지 않습니다.");
        } catch (InvalidLocationException e) {
            reject(session, e.getMessage());
        }
    }

    private void reject(WebSocketSession session, String reason) throws IOException {
        log.debug("위치 메시지 거절: session={}, reason={}", session.getId(), reason);
        session.sendMessage(new TextMessage(jsonMapper.writeValueAsString(java.util.Map.of("error", reason))));
    }
}
