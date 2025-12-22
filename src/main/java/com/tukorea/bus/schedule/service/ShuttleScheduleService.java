package com.tukorea.bus.schedule.service;

import com.tukorea.bus.schedule.dto.FindShuttleScheduleResponse;
import java.util.List;

public interface ShuttleScheduleService {

    List<FindShuttleScheduleResponse> findSchedules(String dayType, String direction);
}

