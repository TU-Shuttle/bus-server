package com.tukorea.bus.ride.dto;

import com.tukorea.bus.ride.entity.RideIntent;
import java.time.LocalDate;
import java.time.LocalTime;

public record RideResponse(Long id, LocalDate serviceDate, String direction, LocalTime departTime, String dropOff) {

    public static RideResponse from(RideIntent ride) {
        return new RideResponse(ride.getId(), ride.getServiceDate(), ride.getDirection().name(), ride.getDepartTime(),
                ride.getDropOff().name());
    }
}
