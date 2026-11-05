package com.tukorea.bus.location.websocket;

import com.tukorea.bus.location.config.RealtimeProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/** 기사 앱만 위치를 올릴 수 있도록 {@code X-Driver-Token} 헤더를 검사한다. 토큰이 설정되지 않았으면 모두 거절한다. */
@Slf4j
@Component
public class DriverHandshakeInterceptor implements HandshakeInterceptor {

    public static final String HEADER = "X-Driver-Token";

    private final byte[] expected;

    public DriverHandshakeInterceptor(RealtimeProperties properties) {
        this.expected = properties.driverToken().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        String provided = request.getHeaders().getFirst(HEADER);
        boolean authorized = expected.length > 0 && provided != null
                && MessageDigest.isEqual(expected, provided.getBytes(StandardCharsets.UTF_8));
        if (!authorized) {
            log.warn("기사 WebSocket 인증 실패: remote={}", request.getRemoteAddress());
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
        }
        return authorized;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                               Exception exception) {
        // no-op
    }
}
