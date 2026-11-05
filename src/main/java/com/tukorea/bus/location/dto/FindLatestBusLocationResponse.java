package com.tukorea.bus.location.dto;

import com.tukorea.bus.location.domain.BusLocation;

public record FindLatestBusLocationResponse(
        String id,
        double lat,
        double lng,
        double accuracy,
        String status,
        long updatedAt
) {

    public static FindLatestBusLocationResponse from(BusLocation location) {
        return new FindLatestBusLocationResponse(location.id(), location.lat(), location.lng(),
                location.accuracy(), location.status().name(), location.updatedAt());
    }
}
