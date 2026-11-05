package com.tukorea.bus.ride.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateRideRequest(
        @NotNull LocalDate serviceDate,
        @NotBlank String direction,
        @NotNull LocalTime departTime,
        @NotBlank String dropOff
) {
}
