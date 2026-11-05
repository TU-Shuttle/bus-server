package com.tukorea.bus.ride.dto;

import java.time.LocalTime;
import java.util.Map;

/** 한 출발편의 탑승 예정 인원과 하차지별 인원. */
public record RideSummaryResponse(String direction, LocalTime departTime, long riders, Map<String, Long> dropOffs) {
}
