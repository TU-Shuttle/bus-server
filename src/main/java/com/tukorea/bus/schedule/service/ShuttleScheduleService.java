package com.tukorea.bus.schedule.service;

import com.tukorea.bus.schedule.dto.FindShuttleScheduleResponse;
import com.tukorea.bus.schedule.dto.FindTimetableResponse;
import java.util.List;

public interface ShuttleScheduleService {

    List<FindShuttleScheduleResponse> findSchedules(String dayType, String direction);

    /** @throws com.tukorea.bus.common.response.exception.BusinessException 활성 시간표가 없으면 RESOURCE_NOT_FOUND */
    FindTimetableResponse findActiveTimetable();
}

