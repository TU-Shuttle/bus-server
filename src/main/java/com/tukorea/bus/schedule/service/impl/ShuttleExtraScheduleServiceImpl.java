package com.tukorea.bus.schedule.service.impl;

import com.tukorea.bus.schedule.dto.ReplaceExtraTimetableRequest;
import com.tukorea.bus.schedule.entity.ShuttleExtraSchedule;
import com.tukorea.bus.schedule.repository.ShuttleExtraScheduleRepository;
import com.tukorea.bus.schedule.service.ShuttleExtraScheduleService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShuttleExtraScheduleServiceImpl implements ShuttleExtraScheduleService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final ShuttleExtraScheduleRepository repository;
    private final Clock clock = Clock.system(SEOUL);

    @Override
    @Transactional
    public int replace(ReplaceExtraTimetableRequest request) {
        repository.deleteAllInBatch();
        List<ShuttleExtraSchedule> rows = request.departures().stream()
                .map(d -> ShuttleExtraSchedule.builder()
                        .title(request.title())
                        .validFrom(request.validFrom())
                        .validTo(request.validTo())
                        .direction(d.direction())
                        .departTime(d.departTime())
                        .daysOfWeek(d.days())
                        .note(d.note())
                        .build())
                .toList();
        repository.saveAll(rows);
        log.info("추가 운행 시간표 교체: title={}, rows={}", request.title(), rows.size());
        return rows.size();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShuttleExtraSchedule> findActive() {
        LocalDate today = LocalDate.now(clock);
        return repository.findAllByOrderByDirectionAscDepartTimeAsc().stream()
                .filter(s -> s.isActiveOn(today))
                .toList();
    }
}
