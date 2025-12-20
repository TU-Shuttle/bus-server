package com.tukorea.bus.websocket.repository;

import com.tukorea.bus.websocket.entity.BusLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusLocationRepository extends JpaRepository<BusLocation, String> {
}
