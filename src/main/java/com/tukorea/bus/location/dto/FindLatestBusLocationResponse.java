package com.tukorea.bus.location.dto;

import com.tukorea.bus.location.domain.BusLocation;

public record FindLatestBusLocationResponse(
        String id,
        double lat,
        double lng,
        double accuracy,
        String status,
        /** 운행 중인 노선(ShuttleDirection 이름). 기사가 고르지 않았으면 null. */
        String direction,
        long updatedAt
) {

    public static FindLatestBusLocationResponse from(BusLocation location) {
        return new FindLatestBusLocationResponse(location.id(), location.lat(), location.lng(),
                location.accuracy(), location.status().name(),
                location.direction() == null ? null : location.direction().name(), location.updatedAt());
    }
}
