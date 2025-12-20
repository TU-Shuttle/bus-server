package com.tukorea.bus.websocket.service;

import com.tukorea.bus.websocket.entity.BusLocation;
import com.tukorea.bus.websocket.repository.BusLocationRepository;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class LocationService {

    private final BusLocationRepository busLocationRepository;
    private final Map<String, BusLocation> latest = new ConcurrentHashMap<>();

    @Transactional
    public void updateBusLocation(String id, BusLocation location) {
        latest.put(id, location);
        busLocationRepository.save(location);
    }

    public BusLocation findLatestBusLocation(String id) {
        BusLocation cached = latest.get(id);
        if (cached != null) {
            return cached;
        }
        BusLocation dbLocation = busLocationRepository.findById(id).orElse(null);
        if (dbLocation != null) {
            latest.put(id, dbLocation);
        }
        return dbLocation;
    }
}
