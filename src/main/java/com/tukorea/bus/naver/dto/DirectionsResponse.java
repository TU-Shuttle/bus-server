package com.tukorea.bus.naver.dto;

import java.util.List;

public record DirectionsResponse(
        List<RouteOption> routes
) {
    public record RouteOption(
            String type,
            List<Path> paths
    ) {
    }

    public record Path(
            List<List<Double>> coordinates,
            Integer totalDistance,
            Integer totalDuration,
            Point start,
            Point goal,
            List<Section> sections
    ) {
    }

    public record Point(
            List<Double> location
    ) {
    }

    public record Section(
            Integer distance,
            Integer congestion,
            String name,
            Integer speed
    ) {
    }
}

