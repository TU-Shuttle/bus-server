package com.tukorea.bus.schedule.parser;

import java.time.LocalDate;
import java.util.List;

public record ParsedTimetable(
        String title,
        LocalDate validFrom,
        LocalDate validTo,
        List<ParsedDeparture> departures
) {
}
