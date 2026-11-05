package com.tukorea.bus.schedule.service.impl;

import com.tukorea.bus.common.converter.ConverterUtil;
import com.tukorea.bus.common.response.code.BusinessErrorCode;
import com.tukorea.bus.common.response.exception.BusinessException;
import com.tukorea.bus.schedule.converter.ShuttleScheduleToDtoConverter;
import com.tukorea.bus.schedule.dto.FindShuttleScheduleResponse;
import com.tukorea.bus.schedule.entity.ShuttleDayType;
import com.tukorea.bus.schedule.entity.ShuttleDirection;
import com.tukorea.bus.schedule.repository.ShuttleScheduleRepository;
import com.tukorea.bus.schedule.service.ShuttleScheduleService;
import java.util.List;
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
    private final ShuttleScheduleToDtoConverter shuttleScheduleToDtoConverter;

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
