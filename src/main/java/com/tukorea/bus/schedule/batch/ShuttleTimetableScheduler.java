package com.tukorea.bus.schedule.batch;

import com.tukorea.bus.schedule.repository.ShuttleTimetableRepository;
import com.tukorea.bus.schedule.service.ShuttleTimetableIngestService;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 학교 페이지의 시간표를 주기적으로 수집한다. 수동 실행은 관리자 API를 쓴다. */
@Slf4j
@Component
@AllArgsConstructor
@ConditionalOnProperty(prefix = "shuttle.timetable", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ShuttleTimetableScheduler {

    private final ShuttleTimetableIngestService ingestService;
    private final ShuttleTimetableRepository timetableRepository;

    @Scheduled(cron = "${shuttle.timetable.refresh-cron:0 0 4 * * *}", zone = "Asia/Seoul")
    public void refreshPeriodically() {
        log.info("시간표 주기 수집 시작");
        ingestService.refreshAll();
    }

    /** 활성 시간표가 하나도 없으면(최초 기동) 바로 한 번 수집한다. 실패해도 기동은 막지 않는다. */
    @EventListener(ApplicationReadyEvent.class)
    public void refreshIfEmpty() {
        if (timetableRepository.existsByActiveTrue()) {
            return;
        }
        log.info("활성 시간표가 없어 기동 직후 수집합니다");
        CompletableFuture.runAsync(ingestService::refreshAll);
    }
}
