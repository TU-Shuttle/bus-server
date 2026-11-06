package com.tukorea.bus.location.service;

import com.tukorea.bus.location.config.RealtimeProperties;
import com.tukorea.bus.location.domain.BusLocation;
import com.tukorea.bus.location.domain.BusStatus;
import com.tukorea.bus.location.stream.LocationStreamHub;
import com.tukorea.bus.schedule.entity.ShuttleDirection;
import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 버스별 최신 위치를 메모리에 보관하고, 갱신될 때마다 구독자에게 푸시한다.
 * 일정 시간 갱신이 없으면(앱 종료, 운행 종료) 조회에서 제외한다.
 */
@Slf4j
@Service
public class LocationService {

    private static final Pattern BUS_ID = Pattern.compile("[A-Za-z0-9_-]{1,32}");
    /** 대한민국 대략적인 위경도 범위. 명백히 잘못된 좌표를 걸러낸다. */
    private static final double MIN_LAT = 33.0;
    private static final double MAX_LAT = 39.0;
    private static final double MIN_LNG = 124.0;
    private static final double MAX_LNG = 132.0;
    private static final int MAX_BUSES = 200;
    private static final int MAX_DEVICE_ID = 64;

    private final Map<String, BusLocation> latest = new ConcurrentHashMap<>();
    private final RealtimeProperties properties;
    private final LocationStreamHub streamHub;
    private final Clock clock;

    public LocationService(RealtimeProperties properties, LocationStreamHub streamHub, Clock clock) {
        this.properties = properties;
        this.streamHub = streamHub;
        this.clock = clock;
    }

    /**
     * @param accuracy GPS 오차(m). 모르면 null
     * @param status    운행 상태. 모르면 null(RUNNING)
     * @param direction 운행 노선(ShuttleDirection 이름). 모르면 null
     * @param deviceId  기사 앱 설치 id. 모르면 null(번호 중복 검사를 하지 않는다)
     * @throws InvalidLocationException 버스 id, 좌표, 상태, 노선이 올바르지 않을 때
     * @throws BusNumberTakenException  다른 기기가 이 번호로 운행 중일 때
     */
    public synchronized BusLocation update(String id, double lat, double lng, Double accuracy, String status,
                                           String direction, String deviceId) {
        if (id == null || !BUS_ID.matcher(id).matches()) {
            throw new InvalidLocationException("올바르지 않은 버스 id 입니다.");
        }
        if (!Double.isFinite(lat) || !Double.isFinite(lng)
                || lat < MIN_LAT || lat > MAX_LAT || lng < MIN_LNG || lng > MAX_LNG) {
            throw new InvalidLocationException("좌표가 허용 범위를 벗어났습니다.");
        }
        if (accuracy != null && (!Double.isFinite(accuracy) || accuracy < 0)) {
            throw new InvalidLocationException("accuracy 가 올바르지 않습니다.");
        }
        String device = deviceId == null || deviceId.isBlank() ? null : deviceId.trim();
        if (device != null && device.length() > MAX_DEVICE_ID) {
            throw new InvalidLocationException("deviceId 가 너무 깁니다.");
        }
        if (!isAvailable(id, device)) {
            throw new BusNumberTakenException(id);
        }
        if (!latest.containsKey(id) && latest.size() >= MAX_BUSES) {
            throw new InvalidLocationException("등록 가능한 버스 수를 초과했습니다.");
        }

        BusLocation location = new BusLocation(id, lat, lng, accuracy == null ? 0 : accuracy,
                parseStatus(status), parseDirection(direction), clock.millis(), device);
        latest.put(id, location);
        streamHub.publish(location);
        return location;
    }

    private BusStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return BusStatus.RUNNING;
        }
        try {
            return BusStatus.valueOf(status.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new InvalidLocationException("status 는 RUNNING, WAITING, STOP 중 하나여야 합니다.");
        }
    }

    private ShuttleDirection parseDirection(String direction) {
        if (direction == null || direction.isBlank()) {
            return null;
        }
        try {
            return ShuttleDirection.valueOf(direction.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new InvalidLocationException("알 수 없는 노선입니다.");
        }
    }

    /**
     * 이 기기가 이 번호로 운행해도 되는지. 다른 기기가 올린 위치가 아직 살아 있고 운행 종료(STOP)가 아니면 쓸 수 없다.
     * 운행을 끝내거나(STOP) 위치가 오래되면(stale-after) 번호가 풀린다. 기기를 모르는 쪽이 있으면 막지 않는다.
     */
    public boolean isAvailable(String id, String deviceId) {
        BusLocation current = latest.get(id);
        if (current == null || !isFresh(current) || current.status() == BusStatus.STOP) {
            return true;
        }
        return current.deviceId() == null || deviceId == null || current.deviceId().equals(deviceId);
    }

    /** 최신 위치. 오래됐거나 없으면 비어 있다. */
    public Optional<BusLocation> findLatest(String id) {
        return Optional.ofNullable(latest.get(id)).filter(this::isFresh);
    }

    public List<BusLocation> findAllFresh() {
        return latest.values().stream()
                .filter(this::isFresh)
                .sorted(Comparator.comparing(BusLocation::id))
                .toList();
    }

    /** 오래된 항목을 정리한다(메모리 누수 방지). 조회는 isFresh 로 이미 걸러진다. */
    @Scheduled(fixedDelay = 60_000)
    void evictStale() {
        long limit = clock.millis() - properties.staleAfter().toMillis() * 10;
        latest.values().removeIf(location -> location.updatedAt() < limit);
    }

    private boolean isFresh(BusLocation location) {
        return clock.millis() - location.updatedAt() <= properties.staleAfter().toMillis();
    }
}
