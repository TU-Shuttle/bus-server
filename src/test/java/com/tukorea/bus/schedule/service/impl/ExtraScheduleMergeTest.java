package com.tukorea.bus.schedule.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.tukorea.bus.schedule.dto.FindShuttleScheduleResponse;
import com.tukorea.bus.schedule.entity.ShuttleDirection;
import com.tukorea.bus.schedule.entity.ShuttleExtraSchedule;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ExtraScheduleMergeTest {

    private static FindShuttleScheduleResponse regular(long id, String time) {
        ShuttleDirection d = ShuttleDirection.STATION_TO_CAMPUS2_VIA_CAMPUS1;
        return new FindShuttleScheduleResponse(id, "WEEKDAY", d.name(), d.getLabel(), LocalTime.parse(time), null, null, null);
    }

    private static ShuttleExtraSchedule extra(long id, String time, DayOfWeek... days) {
        ShuttleExtraSchedule e = ShuttleExtraSchedule.builder()
                .title("t")
                .direction(ShuttleDirection.STATION_TO_CAMPUS2_VIA_CAMPUS1)
                .departTime(LocalTime.parse(time))
                .daysOfWeek(List.of(days))
                .note("조기취업학부 셔틀")
                .build();
        ReflectionTestUtils.setField(e, "id", id);
        return e;
    }

    @Test
    void 같은_시각이면_요일이_구체적인_추가_운행이_남는다() {
        List<FindShuttleScheduleResponse> merged = ShuttleScheduleServiceImpl.mergeExtra(
                List.of(regular(1, "08:55"), regular(2, "09:00")),
                List.of(extra(7, "08:55", DayOfWeek.WEDNESDAY, DayOfWeek.MONDAY), extra(8, "09:05", DayOfWeek.WEDNESDAY)));

        assertThat(merged).extracting(r -> r.departTime().toString())
                .containsExactly("08:55", "09:00", "09:05");
        FindShuttleScheduleResponse first = merged.getFirst();
        assertThat(first.id()).isEqualTo(-7L);
        assertThat(first.daysOfWeek()).containsExactly("MONDAY", "WEDNESDAY");
        assertThat(merged.get(1).daysOfWeek()).isNull();
    }
}
