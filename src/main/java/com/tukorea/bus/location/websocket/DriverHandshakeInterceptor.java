package com.tukorea.bus.location.websocket;

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

    public static final String HEADER = DriverTokenVerifier.HEADER;

    private final DriverTokenVerifier verifier;

    public DriverHandshakeInterceptor(DriverTokenVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        String provided = request.getHeaders().getFirst(HEADER);
        boolean authorized = verifier.verify(provided);
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
