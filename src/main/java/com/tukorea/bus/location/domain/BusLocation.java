package com.tukorea.bus.location.domain;

import com.tukorea.bus.schedule.entity.ShuttleDirection;

/**
 * 버스의 최신 위치. DB에 저장하지 않고 메모리에만 둔다. accuracy 는 GPS 오차(m)이며 모르면 0.
 * direction 은 기사가 운행 시작 때 고른 노선이며, 고르지 않았으면 null.
 * deviceId 는 위치를 올린 기사 앱 설치 하나를 가리키는 값으로, 같은 번호를 두 기사가 쓰지 못하게 할 때만 쓴다(응답에 넣지 않는다).
 */
public record BusLocation(String id, double lat, double lng, double accuracy, BusStatus status,
                          ShuttleDirection direction, long updatedAt, String deviceId) {
}
