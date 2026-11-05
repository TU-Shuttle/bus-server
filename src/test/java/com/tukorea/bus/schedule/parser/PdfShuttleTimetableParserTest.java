package com.tukorea.bus.schedule.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tukorea.bus.schedule.entity.ShuttleDayType;
import com.tukorea.bus.schedule.entity.ShuttleDirection;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

/** 2026학년도 2학기(9.1~12.22) 시간표 PDF를 그대로 사용해 해석 결과를 검증한다. */
class PdfShuttleTimetableParserTest {

    private static ParsedTimetable parsed;

    @BeforeAll
    static void parse() throws IOException {
        byte[] pdf = new ClassPathResource("timetable/shuttle-2026-2.pdf").getContentAsByteArray();
        parsed = new PdfShuttleTimetableParser().parse(pdf, 2026);
    }

    @Test
    void 제목과_적용기간을_읽는다() {
        assertThat(parsed.title()).isEqualTo("2학기 셔틀버스 시간표");
        assertThat(parsed.validFrom()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(parsed.validTo()).isEqualTo(LocalDate.of(2026, 12, 22));
    }

    @Test
    void 학교출발_정시_시간을_모두_읽는다() {
        List<String> times = times(ShuttleDayType.WEEKDAY, ShuttleDirection.CAMPUS1_TO_STATION);
        assertThat(times).hasSize(55);
        assertThat(times).contains("09:00", "09:20", "09:40", "10:05", "16:50", "19:45", "21:48", "22:40");
        assertThat(times).doesNotContain("08:00", "17:00");
    }

    @Test
    void 정왕역출발_정시_시간과_막차를_읽는다() {
        List<String> times = times(ShuttleDayType.WEEKDAY, ShuttleDirection.STATION_TO_CAMPUS1);
        assertThat(times).contains("10:00", "10:15", "10:50", "14:40", "16:50", "22:17");
        assertThat(parsed.departures().stream()
                .filter(d -> d.direction() == ShuttleDirection.STATION_TO_CAMPUS1)
                .filter(d -> d.departTime().equals(LocalTime.of(22, 17)))
                .findFirst().orElseThrow().note()).isEqualTo("막차");
    }

    @Test
    void 수시운행_구간을_읽는다() {
        assertThat(parsed.departures()).anySatisfy(d -> {
            assertThat(d.direction()).isEqualTo(ShuttleDirection.STATION_TO_CAMPUS1);
            assertThat(d.departTime()).isEqualTo(LocalTime.of(8, 40));
            assertThat(d.endTime()).isEqualTo(LocalTime.of(10, 0));
            assertThat(d.note()).isEqualTo("수시운행");
        });
        assertThat(parsed.departures()).anySatisfy(d -> {
            assertThat(d.direction()).isEqualTo(ShuttleDirection.CAMPUS1_TO_STATION);
            assertThat(d.departTime()).isEqualTo(LocalTime.of(17, 0));
            assertThat(d.endTime()).isEqualTo(LocalTime.of(18, 0));
        });
    }

    @Test
    void 제2캠퍼스_평일을_읽는다() {
        assertThat(times(ShuttleDayType.WEEKDAY, ShuttleDirection.CAMPUS1_TO_CAMPUS2))
                .containsExactly("10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00",
                        "19:00");
        assertThat(times(ShuttleDayType.WEEKDAY, ShuttleDirection.STATION_TO_CAMPUS2_VIA_CAMPUS1))
                .containsExactly("08:55", "09:00");
        assertThat(times(ShuttleDayType.WEEKDAY, ShuttleDirection.CAMPUS2_TO_CAMPUS1))
                .containsExactly("09:40", "10:30", "11:30", "12:30", "13:30", "14:30", "15:30", "16:30", "17:40",
                        "18:30", "19:30");
        assertThat(parsed.departures().stream()
                .filter(d -> d.direction() == ShuttleDirection.CAMPUS2_TO_CAMPUS1)
                .filter(d -> d.departTime().equals(LocalTime.of(17, 40)))
                .findFirst().orElseThrow().note()).contains("오이도역");
    }

    @Test
    void 토요일을_읽는다() {
        assertThat(times(ShuttleDayType.SATURDAY, ShuttleDirection.STATION_TO_CAMPUS2_VIA_CAMPUS1))
                .containsExactly("08:45", "08:50", "09:00", "09:05", "09:10", "09:15");
        assertThat(times(ShuttleDayType.SATURDAY, ShuttleDirection.CAMPUS2_TO_STATION_VIA_CAMPUS1))
                .containsExactly("16:30", "16:45", "19:25", "19:28", "19:30", "19:35", "19:45");
    }

    @Test
    void 각주의_시각은_표_데이터로_중복_반영되지_않는다() {
        assertThat(times(ShuttleDayType.WEEKDAY, ShuttleDirection.CAMPUS1_TO_CAMPUS2)).doesNotContain("08:55");
        assertThat(parsed.departures().stream()
                .filter(d -> d.direction() == ShuttleDirection.STATION_TO_CAMPUS2_VIA_CAMPUS1
                        && d.dayType() == ShuttleDayType.WEEKDAY)).hasSize(2);
    }

    @Test
    void PDF가_아니면_해석에_실패한다() {
        byte[] garbage = "not a pdf".getBytes(StandardCharsets.UTF_8);
        assertThatThrownBy(() -> new PdfShuttleTimetableParser().parse(garbage, 2026))
                .isInstanceOf(TimetableParseException.class);
    }

    private static List<String> times(ShuttleDayType dayType, ShuttleDirection direction) {
        Stream<ParsedDeparture> stream = parsed.departures().stream()
                .filter(d -> d.dayType() == dayType && d.direction() == direction)
                .filter(d -> d.endTime() == null || d.note() == null || !d.note().contains("도착"));
        return stream.filter(d -> !"수시운행".equals(d.note()))
                .map(d -> d.departTime().toString()).sorted().toList();
    }
}
