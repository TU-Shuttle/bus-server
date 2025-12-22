package com.tukorea.bus.naver.dto;

import jakarta.validation.constraints.NotNull;

public record DirectionsRequest(
        @NotNull(message = "출발지 좌표는 필수입니다.") String start,
        String waypoints,
        @NotNull(message = "목적지 좌표는 필수입니다.") String end,
        String option,
        Integer cartype
) {
}

