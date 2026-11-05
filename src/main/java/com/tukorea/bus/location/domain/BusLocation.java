package com.tukorea.bus.location.domain;

/** 버스의 최신 위치. DB에 저장하지 않고 메모리에만 둔다. */
public record BusLocation(String id, double lat, double lng, long updatedAt) {
}
