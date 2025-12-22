package com.tukorea.bus.schedule.dto;

import java.time.LocalTime;

public record FindShuttleScheduleResponse(
        Long id,
        String dayType,
        String direction,
        LocalTime departTime,
        String note
) {
}

