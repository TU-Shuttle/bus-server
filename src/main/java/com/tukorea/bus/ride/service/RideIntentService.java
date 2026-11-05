package com.tukorea.bus.ride.service;

import com.tukorea.bus.common.response.code.BusinessErrorCode;
import com.tukorea.bus.common.response.exception.BusinessException;
import com.tukorea.bus.ride.dto.CreateRideRequest;
import com.tukorea.bus.ride.dto.RideResponse;
import com.tukorea.bus.ride.dto.RideSummaryResponse;
import com.tukorea.bus.ride.entity.DropOffRules;
import com.tukorea.bus.ride.entity.RideIntent;
import com.tukorea.bus.ride.entity.ShuttleStop;
import com.tukorea.bus.ride.repository.RideIntentRepository;
import com.tukorea.bus.schedule.entity.ShuttleDayType;
import com.tukorea.bus.schedule.entity.ShuttleDirection;
import com.tukorea.bus.schedule.repository.ShuttleScheduleRepository;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RideIntentService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final Pattern DEVICE_ID = Pattern.compile("[A-Za-z0-9-]{8,64}");
    private static final int MAX_DAYS_AHEAD = 7;
    private static final int MAX_ACTIVE_PER_DEVICE = 10;

    private final RideIntentRepository rideIntentRepository;
    private final ShuttleScheduleRepository shuttleScheduleRepository;
    private final Clock clock;

    /** 같은 출발편에 다시 요청하면 하차지만 바꾼다. */
    @Transactional
    public RideResponse reserve(String deviceId, CreateRideRequest request) {
        validateDevice(deviceId);
        ShuttleDirection direction = parse(ShuttleDirection.class, request.direction());
        ShuttleStop dropOff = parse(ShuttleStop.class, request.dropOff());
        LocalDate date = request.serviceDate();
        LocalTime time = request.departTime().withSecond(0).withNano(0);

        validateDeparture(date, direction, time);
        if (!DropOffRules.allows(direction, dropOff)) {
            throw new BusinessException(BusinessErrorCode.INVALID_REQUEST);
        }

        RideIntent existing = rideIntentRepository
                .findByDeviceIdAndServiceDateAndDirectionAndDepartTime(deviceId, date, direction, time)
                .orElse(null);
        if (existing != null) {
            existing.changeDropOff(dropOff);
            return RideResponse.from(existing);
        }
        if (rideIntentRepository.countByDeviceIdAndServiceDateGreaterThanEqual(deviceId, today())
                >= MAX_ACTIVE_PER_DEVICE) {
            throw new BusinessException(BusinessErrorCode.INVALID_REQUEST);
        }

        return RideResponse.from(rideIntentRepository.save(RideIntent.builder()
                .deviceId(deviceId)
                .serviceDate(date)
                .direction(direction)
                .departTime(time)
                .dropOff(dropOff)
                .createdAt(LocalDateTime.now(clock.withZone(SEOUL)))
                .build()));
    }

    @Transactional(readOnly = true)
    public List<RideResponse> findMine(String deviceId) {
        validateDevice(deviceId);
        return rideIntentRepository
                .findByDeviceIdAndServiceDateGreaterThanEqualOrderByServiceDateAscDepartTimeAsc(deviceId, today())
                .stream().map(RideResponse::from).toList();
    }

    @Transactional
    public void cancel(String deviceId, Long id) {
        validateDevice(deviceId);
        RideIntent ride = rideIntentRepository.findById(id)
                .filter(r -> r.getDeviceId().equals(deviceId))
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.RESOURCE_NOT_FOUND));
        rideIntentRepository.delete(ride);
    }

    @Transactional(readOnly = true)
    public List<RideSummaryResponse> summarize(LocalDate date) {
        Map<String, RideSummaryBuilder> byDeparture = new LinkedHashMap<>();
        for (RideIntentRepository.RideCount count : rideIntentRepository.countByDate(date)) {
            String key = count.getDirection() + "@" + count.getDepartTime();
            byDeparture.computeIfAbsent(key, k -> new RideSummaryBuilder(count.getDirection(), count.getDepartTime()))
                    .add(count.getDropOff(), count.getRiders());
        }
        return byDeparture.values().stream().map(RideSummaryBuilder::build).toList();
    }

    private void validateDeparture(LocalDate date, ShuttleDirection direction, LocalTime time) {
        LocalDate today = today();
        if (date.isBefore(today) || date.isAfter(today.plusDays(MAX_DAYS_AHEAD))) {
            throw new BusinessException(BusinessErrorCode.INVALID_REQUEST);
        }
        if (date.equals(today) && time.isBefore(LocalTime.now(clock.withZone(SEOUL)).withSecond(0).withNano(0))) {
            throw new BusinessException(BusinessErrorCode.INVALID_REQUEST);
        }
        ShuttleDayType dayType = dayTypeOf(date);
        boolean exists = shuttleScheduleRepository.findActive(dayType, direction).stream()
                .anyMatch(s -> s.getEndTime() == null && s.getDepartTime().equals(time));
        if (!exists) {
            throw new BusinessException(BusinessErrorCode.RESOURCE_NOT_FOUND);
        }
    }

    private ShuttleDayType dayTypeOf(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SUNDAY) {
            throw new BusinessException(BusinessErrorCode.INVALID_REQUEST);
        }
        return day == DayOfWeek.SATURDAY ? ShuttleDayType.SATURDAY : ShuttleDayType.WEEKDAY;
    }

    private LocalDate today() {
        return LocalDate.now(clock.withZone(SEOUL));
    }

    private void validateDevice(String deviceId) {
        if (deviceId == null || !DEVICE_ID.matcher(deviceId).matches()) {
            throw new BusinessException(BusinessErrorCode.INVALID_REQUEST);
        }
    }

    private <E extends Enum<E>> E parse(Class<E> type, String value) {
        try {
            return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(BusinessErrorCode.INVALID_REQUEST);
        }
    }

    private static final class RideSummaryBuilder {

        private final ShuttleDirection direction;
        private final LocalTime departTime;
        private final Map<String, Long> dropOffs = new LinkedHashMap<>();
        private long riders;

        private RideSummaryBuilder(ShuttleDirection direction, LocalTime departTime) {
            this.direction = direction;
            this.departTime = departTime;
        }

        private void add(ShuttleStop dropOff, long count) {
            dropOffs.merge(dropOff.name(), count, Long::sum);
            riders += count;
        }

        private RideSummaryResponse build() {
            return new RideSummaryResponse(direction.name(), departTime, riders, Map.copyOf(dropOffs));
        }
    }
}
