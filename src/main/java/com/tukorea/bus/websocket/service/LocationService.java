package com.tukorea.bus.websocket.service;

import com.tukorea.bus.websocket.entity.BusLocation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

    private final Map<String, BusLocation> latest = new ConcurrentHashMap<>();

    public void updateBusLocation(String id, BusLocation location) {
        latest.put(id, location);
    }

    public BusLocation findLatestBusLocation(String id) {
        return latest.get(id);
    }
}
