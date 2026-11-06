package com.tukorea.bus.schedule.service.impl;

import com.tukorea.bus.common.converter.ConverterUtil;
import com.tukorea.bus.common.response.code.BusinessErrorCode;
import com.tukorea.bus.common.response.exception.BusinessException;
import com.tukorea.bus.schedule.converter.ShuttleScheduleToDtoConverter;
import com.tukorea.bus.schedule.dto.FindShuttleScheduleResponse;
import com.tukorea.bus.schedule.dto.FindTimetableResponse;
import com.tukorea.bus.schedule.entity.ShuttleExtraSchedule;
import com.tukorea.bus.schedule.entity.ShuttleTimetable;
import com.tukorea.bus.schedule.service.ShuttleExtraScheduleService;
import com.tukorea.bus.schedule.repository.ShuttleTimetableRepository;
import com.tukorea.bus.schedule.entity.ShuttleDayType;
import com.tukorea.bus.schedule.entity.ShuttleDirection;
import com.tukorea.bus.schedule.repository.ShuttleScheduleRepository;
import com.tukorea.bus.schedule.service.ShuttleScheduleService;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class ShuttleScheduleServiceImpl implements ShuttleScheduleService {

    private final ShuttleScheduleRepository shuttleScheduleRepository;
    private final ShuttleTimetableRepository shuttleTimetableRepository;
    private final ShuttleScheduleToDtoConverter shuttleScheduleToDtoConverter;
    private final ShuttleExtraScheduleService extraScheduleService;

    @Override
    @Transactional(readOnly = true)
    public List<FindShuttleScheduleResponse> findSchedules(String dayType, String direction) {
        log.debug("셔틀버스 시간표 조회: dayType={}, direction={}", dayType, direction);

        ShuttleDayType dayTypeFilter = parse(ShuttleDayType.class, dayType);
        ShuttleDirection directionFilter = parse(ShuttleDirection.class, direction);
        return ConverterUtil.convertList(
                shuttleScheduleRepository.findActive(dayTypeFilter, directionFilter),
                shuttleScheduleToDtoConverter);
    }

    @Override
    @Transactional(readOnly = true)
    public FindTimetableResponse findActiveTimetable() {
        ShuttleTimetable timetable = shuttleTimetableRepository.findFirstByActiveTrueOrderByFetchedAtDesc()
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.RESOURCE_NOT_FOUND));
        List<FindShuttleScheduleResponse> regular = ConverterUtil.convertList(
                shuttleScheduleRepository.findActive(null, null), shuttleScheduleToDtoConverter);
        List<FindShuttleScheduleResponse> schedules = mergeExtra(regular, extraScheduleService.findActive());
        return new FindTimetableResponse(timetable.getTitle(), timetable.getValidFrom(), timetable.getValidTo(),
                timetable.getFetchedAt(), schedules);
    }

    /**
     * 요일별 추가 운행을 정규 시간표에 합친다. 같은 방향·같은 시각이 정규 시간표에도 있으면
     * 추가 운행 쪽(요일이 더 구체적)을 남긴다.
     */
    static List<FindShuttleScheduleResponse> mergeExtra(List<FindShuttleScheduleResponse> regular,
                                                        List<ShuttleExtraSchedule> extra) {
        Set<String> overridden = extra.stream()
                .map(e -> key(e.dayType().name(), e.getDirection().name(), e.getDepartTime()))
                .collect(Collectors.toSet());
        List<FindShuttleScheduleResponse> merged = new ArrayList<>(regular.stream()
                .filter(r -> r.endTime() != null || !overridden.contains(key(r.dayType(), r.direction(), r.departTime())))
                .toList());
        extra.forEach(e -> merged.add(new FindShuttleScheduleResponse(
                -e.getId(),
                e.dayType().name(),
                e.getDirection().name(),
                e.getDirection().getLabel(),
                e.getDepartTime(),
                null,
                e.getNote(),
                e.days().stream().map(DayOfWeek::name).toList())));
        merged.sort(Comparator.comparing(FindShuttleScheduleResponse::dayType)
                .thenComparing(FindShuttleScheduleResponse::direction)
                .thenComparing(FindShuttleScheduleResponse::departTime));
        return merged;
    }

    private static String key(String dayType, String direction, LocalTime time) {
        return dayType + "|" + direction + "|" + time;
    }

    private <E extends Enum<E>> E parse(Class<E> type, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BusinessException(BusinessErrorCode.INVALID_REQUEST);
        }
    }
}
