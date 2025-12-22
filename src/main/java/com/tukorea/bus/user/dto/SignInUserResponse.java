package com.tukorea.bus.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignInUserResponse(

        @NotBlank
        @Size(min = 1, max = 250)
        String id
) {
}