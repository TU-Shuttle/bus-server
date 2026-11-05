package com.tukorea.bus.ride;

import static org.assertj.core.api.Assertions.assertThat;

import com.tukorea.bus.schedule.entity.ShuttleDayType;
import com.tukorea.bus.schedule.entity.ShuttleDirection;
import com.tukorea.bus.schedule.entity.ShuttleSchedule;
import com.tukorea.bus.schedule.entity.ShuttleTimetable;
import com.tukorea.bus.schedule.repository.ShuttleScheduleRepository;
import com.tukorea.bus.schedule.repository.ShuttleTimetableRepository;
import com.tukorea.bus.ride.repository.RideIntentRepository;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RideIntentIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ShuttleTimetableRepository timetableRepository;
    @Autowired
    private ShuttleScheduleRepository scheduleRepository;
    @Autowired
    private RideIntentRepository rideIntentRepository;

    private final HttpClient client = HttpClient.newHttpClient();
    private LocalDate weekday;
    private LocalDate saturday;

    @BeforeEach
    void setUp() {
        ShuttleTimetable timetable = timetableRepository.save(ShuttleTimetable.builder()
                .title("테스트 시간표").sourceUrl("test").sourceHash(UUID.randomUUID().toString().replace("-", ""))
                .fetchedAt(LocalDateTime.now()).active(true).build());
        scheduleRepository.save(schedule(timetable, ShuttleDayType.WEEKDAY, ShuttleDirection.STATION_TO_CAMPUS1, "10:00"));
        scheduleRepository.save(schedule(timetable, ShuttleDayType.SATURDAY,
                ShuttleDirection.STATION_TO_CAMPUS2_VIA_CAMPUS1, "09:00"));

        LocalDate tomorrow = LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(1);
        weekday = next(tomorrow, false);
        saturday = next(tomorrow, true);
    }

    @AfterEach
    void tearDown() {
        rideIntentRepository.deleteAll();
        scheduleRepository.deleteAll();
        timetableRepository.deleteAll();
    }

    @Test
    void 탑승_예정을_등록하면_인원이_집계되고_취소할_수_있다() throws Exception {
        String me = UUID.randomUUID().toString();
        String other = UUID.randomUUID().toString();

        HttpResponse<String> created = post(me, weekday, "STATION_TO_CAMPUS1", "10:00", "CAMPUS1");
        assertThat(created.statusCode()).isEqualTo(201);
        assertThat(post(other, weekday, "STATION_TO_CAMPUS1", "10:00", "CAMPUS1").statusCode()).isEqualTo(201);

        String summary = get("/api/rides/summary?date=" + weekday, null).body();
        assertThat(summary).contains("\"riders\":2").contains("\"CAMPUS1\":2");

        String mine = get("/api/rides/mine", me).body();
        assertThat(mine).contains("STATION_TO_CAMPUS1");
        long id = Long.parseLong(mine.replaceAll("(?s).*\"id\":(\\d+).*", "$1"));

        assertThat(delete(id, other).statusCode()).isEqualTo(404);
        assertThat(delete(id, me).statusCode()).isEqualTo(200);
        assertThat(get("/api/rides/summary?date=" + weekday, null).body()).contains("\"riders\":1");
    }

    @Test
    void 같은_출발편에_다시_보내면_하차지만_바뀐다() throws Exception {
        String me = UUID.randomUUID().toString();
        post(me, saturday, "STATION_TO_CAMPUS2_VIA_CAMPUS1", "09:00", "CAMPUS2");
        post(me, saturday, "STATION_TO_CAMPUS2_VIA_CAMPUS1", "09:00", "CAMPUS1_WEST_GATE");

        String summary = get("/api/rides/summary?date=" + saturday, null).body();
        assertThat(summary).contains("\"riders\":1").contains("CAMPUS1_WEST_GATE").doesNotContain("\"CAMPUS2\"");
    }

    @Test
    void 시간표에_없는_출발편이나_갈_수_없는_하차지는_거절된다() throws Exception {
        String me = UUID.randomUUID().toString();
        assertThat(post(me, weekday, "STATION_TO_CAMPUS1", "10:05", "CAMPUS1").statusCode()).isEqualTo(404);
        assertThat(post(me, weekday, "STATION_TO_CAMPUS1", "10:00", "CAMPUS2").statusCode()).isEqualTo(400);
        assertThat(post(me, LocalDate.now().minusDays(2), "STATION_TO_CAMPUS1", "10:00", "CAMPUS1").statusCode())
                .isEqualTo(400);
    }

    @Test
    void 기기_id가_없으면_400() throws Exception {
        HttpResponse<String> response = client.send(HttpRequest.newBuilder(uri("/api/rides/mine")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(400);
    }

    // ---- helpers ----

    private ShuttleSchedule schedule(ShuttleTimetable timetable, ShuttleDayType dayType, ShuttleDirection direction,
                                     String time) {
        return ShuttleSchedule.builder().timetable(timetable).dayType(dayType).direction(direction)
                .departTime(LocalTime.parse(time)).build();
    }

    private LocalDate next(LocalDate from, boolean saturday) {
        LocalDate date = from;
        while (saturday ? date.getDayOfWeek() != DayOfWeek.SATURDAY
                : date.getDayOfWeek().getValue() > 5) {
            date = date.plusDays(1);
        }
        return date;
    }

    private HttpResponse<String> post(String device, LocalDate date, String direction, String time, String dropOff)
            throws Exception {
        String body = "{\"serviceDate\":\"%s\",\"direction\":\"%s\",\"departTime\":\"%s\",\"dropOff\":\"%s\"}"
                .formatted(date, direction, time, dropOff);
        return client.send(HttpRequest.newBuilder(uri("/api/rides"))
                .header("Content-Type", "application/json")
                .header("X-Device-Id", device)
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path, String device) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri(path)).GET();
        if (device != null) {
            builder.header("X-Device-Id", device);
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> delete(long id, String device) throws Exception {
        return client.send(HttpRequest.newBuilder(uri("/api/rides/" + id)).header("X-Device-Id", device).DELETE()
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }
}
