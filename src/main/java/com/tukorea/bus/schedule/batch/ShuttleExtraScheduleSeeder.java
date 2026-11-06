package com.tukorea.bus.schedule.batch;

import com.tukorea.bus.schedule.dto.ReplaceExtraTimetableRequest;
import com.tukorea.bus.schedule.repository.ShuttleExtraScheduleRepository;
import com.tukorea.bus.schedule.service.ShuttleExtraScheduleService;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * 추가 운행 시간표가 비어 있으면 앱에 넣어 둔 기본값(학사 공지로 받은 26-2학기 제2캠퍼스 셔틀)을 넣는다.
 * 이후 학기에는 관리자 API(PUT /api/admin/schedule/extra)로 시각만 바꾼다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShuttleExtraScheduleSeeder {

    private final ShuttleExtraScheduleRepository repository;
    private final ShuttleExtraScheduleService service;
    private final JsonMapper jsonMapper;

    @Value("classpath:timetable/extra-schedule.json")
    private Resource seed;

    @EventListener(ApplicationReadyEvent.class)
    public void seedIfEmpty() {
        if (repository.count() > 0 || !seed.exists()) {
            return;
        }
        try (InputStream in = seed.getInputStream()) {
            int rows = service.replace(jsonMapper.readValue(in, ReplaceExtraTimetableRequest.class));
            log.info("추가 운행 기본 시간표를 넣었습니다: {}줄", rows);
        } catch (IOException | RuntimeException e) {
            log.warn("추가 운행 기본 시간표를 넣지 못했습니다", e);
        }
    }
}
