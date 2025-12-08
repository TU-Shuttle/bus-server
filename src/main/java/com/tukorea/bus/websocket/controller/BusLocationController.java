package com.tukorea.bus.websocket.controller;

import com.tukorea.bus.websocket.dto.FindLatestBusLocationResponse;
import com.tukorea.bus.websocket.entity.BusLocation;
import com.tukorea.bus.websocket.service.LocationService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class BusLocationController {

    private final LocationService locationService;

    @GetMapping("/bus/{id}/location")
    public FindLatestBusLocationResponse findLatestBusLocation(@PathVariable String id) {

        BusLocation entity = locationService.findLatestBusLocation(id);
        if (entity == null) {
            return null;
        }

        return new FindLatestBusLocationResponse(
                entity.getId(),
                entity.getLat(),
                entity.getLng(),
                entity.getUpdatedAt()
        );
    }
}
