package com.tukorea.bus.location.domain;

/** 버스의 최신 위치. DB에 저장하지 않고 메모리에만 둔다. accuracy 는 GPS 오차(m)이며 모르면 0. */
public record BusLocation(String id, double lat, double lng, double accuracy, BusStatus status, long updatedAt) {
}
