package com.tukorea.bus.location.service;

import com.tukorea.bus.location.config.RealtimeProperties;
import com.tukorea.bus.location.domain.BusLocation;
import com.tukorea.bus.location.stream.LocationStreamHub;
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

    private final Map<String, BusLocation> latest = new ConcurrentHashMap<>();
    private final RealtimeProperties properties;
    private final LocationStreamHub streamHub;
    private final Clock clock;

    public LocationService(RealtimeProperties properties, LocationStreamHub streamHub, Clock clock) {
        this.properties = properties;
        this.streamHub = streamHub;
        this.clock = clock;
    }

    /** @throws InvalidLocationException 버스 id나 좌표가 올바르지 않을 때 */
    public BusLocation update(String id, double lat, double lng) {
        if (id == null || !BUS_ID.matcher(id).matches()) {
            throw new InvalidLocationException("올바르지 않은 버스 id 입니다.");
        }
        if (!Double.isFinite(lat) || !Double.isFinite(lng)
                || lat < MIN_LAT || lat > MAX_LAT || lng < MIN_LNG || lng > MAX_LNG) {
            throw new InvalidLocationException("좌표가 허용 범위를 벗어났습니다.");
        }
        if (!latest.containsKey(id) && latest.size() >= MAX_BUSES) {
            throw new InvalidLocationException("등록 가능한 버스 수를 초과했습니다.");
        }

        BusLocation location = new BusLocation(id, lat, lng, clock.millis());
        latest.put(id, location);
        streamHub.publish(location);
        return location;
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
