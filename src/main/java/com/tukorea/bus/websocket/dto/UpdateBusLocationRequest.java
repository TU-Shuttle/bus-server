package com.tukorea.bus.websocket.dto;

public record UpdateBusLocationRequest(
        String id,
        double lat,
        double lng,
        float accuracy,
        String status
) {
}
