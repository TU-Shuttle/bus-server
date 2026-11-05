package com.tukorea.bus.location.dto;

/** 기사 앱이 WebSocket으로 보내는 위치 메시지. */
public record UpdateBusLocationRequest(String id, double lat, double lng) {
}
