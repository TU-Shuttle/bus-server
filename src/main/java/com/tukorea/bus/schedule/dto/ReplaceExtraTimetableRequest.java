package com.tukorea.bus.schedule.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tukorea.bus.schedule.entity.ShuttleDirection;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 요일별 추가 운행 시간표를 통째로 바꾼다. 학기가 바뀌면 시각만 고쳐 다시 올리면 된다.
 * 예: {"title":"제2캠퍼스 셔틀 (26-2학기)","validFrom":"2026-09-01","validTo":"2026-12-22",
 *      "departures":[{"direction":"STATION_TO_CAMPUS2_VIA_CAMPUS1","departTime":"08:55","days":["MONDAY","WEDNESDAY"]}]}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ReplaceExtraTimetableRequest(
        @NotBlank @Size(max = 100) String title,
        LocalDate validFrom,
        LocalDate validTo,
        @NotNull @Valid List<Departure> departures
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Departure(
            @NotNull ShuttleDirection direction,
            @NotNull LocalTime departTime,
            @NotEmpty List<DayOfWeek> days,
            @Size(max = 200) String note
    ) {
    }
}
