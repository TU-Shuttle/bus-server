package com.tukorea.bus.location.domain;

import com.tukorea.bus.schedule.entity.ShuttleDirection;

/**
 * 버스의 최신 위치. DB에 저장하지 않고 메모리에만 둔다. accuracy 는 GPS 오차(m)이며 모르면 0.
 * direction 은 기사가 운행 시작 때 고른 노선이며, 고르지 않았으면 null.
 */
public record BusLocation(String id, double lat, double lng, double accuracy, BusStatus status,
                          ShuttleDirection direction, long updatedAt) {
}
