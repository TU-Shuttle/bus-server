package com.tukorea.bus.websocket.dto;

public record FindLatestBusLocationResponse(
        String id,
        double lat,
        double lng,
        long updatedAt
) {
}
