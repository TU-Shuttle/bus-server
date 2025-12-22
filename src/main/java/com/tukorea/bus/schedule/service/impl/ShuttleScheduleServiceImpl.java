package com.tukorea.bus.schedule.service.impl;

import com.tukorea.bus.common.converter.ConverterUtil;
import com.tukorea.bus.schedule.converter.ShuttleScheduleToDtoConverter;
import com.tukorea.bus.schedule.dto.FindShuttleScheduleResponse;
import com.tukorea.bus.schedule.entity.ShuttleSchedule;
import com.tukorea.bus.schedule.repository.ShuttleScheduleRepository;
import com.tukorea.bus.schedule.service.ShuttleScheduleService;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class ShuttleScheduleServiceImpl implements ShuttleScheduleService {

    private final ShuttleScheduleRepository shuttleScheduleRepository;
    private final ShuttleScheduleToDtoConverter shuttleScheduleToDtoConverter;

    @Override
    @Transactional(readOnly = true)
    public List<FindShuttleScheduleResponse> findSchedules(String dayType, String direction) {
        log.debug("셔틀버스 시간표 조회: dayType={}, direction={}", dayType, direction);

        List<ShuttleSchedule> schedules = retrieveSchedules(dayType, direction);
        return ConverterUtil.convertList(schedules, shuttleScheduleToDtoConverter);
    }

    private List<ShuttleSchedule> retrieveSchedules(String dayType, String direction) {
        if (dayType == null && direction == null) {
            log.debug("전체 셔틀버스 시간표 조회");
            return shuttleScheduleRepository.findAllByOrderByDayTypeAscDirectionAscDepartTimeAsc();
        }

        if (dayType != null && direction != null) {
            log.debug("필터링 조회: dayType={}, direction={}", dayType, direction);
            return shuttleScheduleRepository.findByDayTypeAndDirectionOrderByDepartTimeAsc(dayType, direction);
        }

        log.debug("일부 파라미터만 제공되어 전체 조회로 처리: dayType={}, direction={}", dayType, direction);
        return shuttleScheduleRepository.findAllByOrderByDayTypeAscDirectionAscDepartTimeAsc();
    }
}

