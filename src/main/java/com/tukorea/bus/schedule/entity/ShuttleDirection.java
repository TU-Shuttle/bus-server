package com.tukorea.bus.schedule.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ShuttleDirection {
    STATION_TO_CAMPUS1("정왕역 → 본교"),
    CAMPUS1_TO_STATION("본교 → 정왕역"),
    CAMPUS1_TO_CAMPUS2("본교 → 제2캠퍼스"),
    CAMPUS2_TO_CAMPUS1("제2캠퍼스 → 본교"),
    STATION_TO_CAMPUS2_VIA_CAMPUS1("정왕역 → 본교 → 제2캠퍼스"),
    CAMPUS2_TO_STATION_VIA_CAMPUS1("제2캠퍼스 → 본교 → 정왕역");

    private final String label;
}
