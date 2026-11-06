package com.tukorea.bus.location.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * @param driverToken 기사 앱이 WebSocket 연결 시 {@code X-Driver-Token} 헤더로 보내는 토큰. 비어 있으면 모든 연결을 거절한다
 * @param staleAfter  마지막 위치 수신 후 이 시간이 지나면 '운행 정보 없음'으로 본다
 */
@ConfigurationProperties(prefix = "shuttle.realtime")
public record RealtimeProperties(
        @DefaultValue("") String driverToken,
        @DefaultValue("60s") Duration staleAfter
) {
}
