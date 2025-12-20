package com.tukorea.bus.websocket.handler;

import com.tukorea.bus.websocket.dto.UpdateBusLocationRequest;
import com.tukorea.bus.websocket.entity.BusLocation;
import com.tukorea.bus.websocket.service.LocationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.json.JsonMapper;

@Component
@AllArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private final LocationService locationService;
    private final JsonMapper jsonMapper = new JsonMapper();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        UpdateBusLocationRequest request = jsonMapper.readValue(message.getPayload(), UpdateBusLocationRequest.class);
        BusLocation busLocation = new BusLocation(request.id(), request.lat(), request.lng(),
                request.accuracy(), System.currentTimeMillis());
        locationService.updateBusLocation(request.id(), busLocation);
    }
}
