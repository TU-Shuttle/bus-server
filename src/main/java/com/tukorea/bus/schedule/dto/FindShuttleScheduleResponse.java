package com.tukorea.bus.schedule.dto;

import java.time.LocalTime;

/**
 * @param endTime null이면 정시 출발, 값이 있으면 {@code departTime}~{@code endTime} 수시운행 구간
 */
public record FindShuttleScheduleResponse(
        Long id,
        String dayType,
        String direction,
        String directionLabel,
        LocalTime departTime,
        LocalTime endTime,
        String note
) {
}
