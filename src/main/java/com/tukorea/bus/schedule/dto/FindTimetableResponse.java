package com.tukorea.bus.schedule.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** 현재 적용 중인 시간표 전체(메타데이터 + 모든 출발 시각). 앱이 한 번에 받아 캐시한다. */
public record FindTimetableResponse(
        String title,
        LocalDate validFrom,
        LocalDate validTo,
        LocalDateTime fetchedAt,
        List<FindShuttleScheduleResponse> schedules
) {
}
