package com.tukorea.bus.schedule.parser;

import com.tukorea.bus.schedule.entity.ShuttleDayType;
import com.tukorea.bus.schedule.entity.ShuttleDirection;
import java.time.LocalTime;

public record ParsedDeparture(
        ShuttleDayType dayType,
        ShuttleDirection direction,
        LocalTime departTime,
        LocalTime endTime,
        String note
) {
}
