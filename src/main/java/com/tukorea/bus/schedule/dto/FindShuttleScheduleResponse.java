package com.tukorea.bus.schedule.dto;

import java.time.LocalTime;
import java.util.List;

/**
 * @param endTime null이면 정시 출발, 값이 있으면 {@code departTime}~{@code endTime} 수시운행 구간
 * @param daysOfWeek null이면 dayType의 모든 요일, 값이 있으면 그 요일에만 운행("MONDAY" …)
 */
public record FindShuttleScheduleResponse(
        Long id,
        String dayType,
        String direction,
        String directionLabel,
        LocalTime departTime,
        LocalTime endTime,
        String note,
        List<String> daysOfWeek
) {
}
