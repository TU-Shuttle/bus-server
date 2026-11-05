package com.tukorea.bus.common.config;

import com.tukorea.bus.location.websocket.DriverHandshakeInterceptor;
import com.tukorea.bus.location.websocket.DriverLocationHandler;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/** 기사 앱 전용 위치 업링크. 브라우저 Origin 은 허용하지 않고(기본), 토큰으로 인증한다. */
@Configuration
@EnableWebSocket
@AllArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final DriverLocationHandler driverLocationHandler;
    private final DriverHandshakeInterceptor driverHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(driverLocationHandler, "/ws")
                .addInterceptors(driverHandshakeInterceptor);
    }
}
