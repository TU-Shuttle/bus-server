package com.tukorea.bus.schedule.converter;

import com.tukorea.bus.common.converter.Converter;
import com.tukorea.bus.schedule.dto.FindShuttleScheduleResponse;
import com.tukorea.bus.schedule.entity.ShuttleSchedule;
import org.springframework.stereotype.Component;

@Component
public class ShuttleScheduleToDtoConverter implements Converter<ShuttleSchedule, FindShuttleScheduleResponse> {

    @Override
    public FindShuttleScheduleResponse convert(ShuttleSchedule source) {
        return new FindShuttleScheduleResponse(
                source.getId(),
                source.getDayType().name(),
                source.getDirection().name(),
                source.getDirection().getLabel(),
                source.getDepartTime(),
                source.getEndTime(),
                source.getNote(),
                null
        );
    }
}
