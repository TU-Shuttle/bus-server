package com.tukorea.bus.location.websocket;

import com.tukorea.bus.location.config.RealtimeProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.stereotype.Component;

/** 기사 앱 토큰({@code X-Driver-Token}) 검사. 토큰이 설정되지 않았으면 모두 거절한다. */
@Component
public class DriverTokenVerifier {

    public static final String HEADER = "X-Driver-Token";

    private final byte[] expected;

    public DriverTokenVerifier(RealtimeProperties properties) {
        this.expected = properties.driverToken().getBytes(StandardCharsets.UTF_8);
    }

    public boolean verify(String provided) {
        return expected.length > 0 && provided != null
                && MessageDigest.isEqual(expected, provided.getBytes(StandardCharsets.UTF_8));
    }
}
