package com.tukorea.bus.location.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 기사 앱이 WebSocket으로 보내는 위치 메시지. accuracy/status/direction 은 생략할 수 있고(status 기본 RUNNING),
 * 모르는 필드는 무시한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UpdateBusLocationRequest(String id, double lat, double lng, Double accuracy, String status,
                                       String direction) {
}
