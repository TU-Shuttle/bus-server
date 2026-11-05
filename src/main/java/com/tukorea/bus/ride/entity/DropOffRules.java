package com.tukorea.bus.ride.entity;

import com.tukorea.bus.schedule.entity.ShuttleDirection;
import java.util.List;
import java.util.Map;

/** 방향별로 내릴 수 있는 곳. 경유 노선은 중간 정류장(본교 서문)에서도 내릴 수 있다. */
public final class DropOffRules {

    private static final Map<ShuttleDirection, List<ShuttleStop>> OPTIONS = Map.of(
            ShuttleDirection.STATION_TO_CAMPUS1, List.of(ShuttleStop.CAMPUS1),
            ShuttleDirection.CAMPUS1_TO_STATION, List.of(ShuttleStop.JEONGWANG_STATION),
            ShuttleDirection.CAMPUS1_TO_CAMPUS2, List.of(ShuttleStop.CAMPUS2),
            ShuttleDirection.CAMPUS2_TO_CAMPUS1, List.of(ShuttleStop.CAMPUS1),
            ShuttleDirection.STATION_TO_CAMPUS2_VIA_CAMPUS1,
            List.of(ShuttleStop.CAMPUS1_WEST_GATE, ShuttleStop.CAMPUS2),
            ShuttleDirection.CAMPUS2_TO_STATION_VIA_CAMPUS1,
            List.of(ShuttleStop.CAMPUS1_WEST_GATE, ShuttleStop.JEONGWANG_STATION)
    );

    private DropOffRules() {
    }

    public static List<ShuttleStop> optionsFor(ShuttleDirection direction) {
        return OPTIONS.getOrDefault(direction, List.of());
    }

    public static boolean allows(ShuttleDirection direction, ShuttleStop dropOff) {
        return optionsFor(direction).contains(dropOff);
    }
}
